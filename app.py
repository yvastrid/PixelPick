from flask import Flask, render_template, request, jsonify, redirect, url_for, session
from flask_login import LoginManager, login_user, logout_user, login_required, current_user
from flask_mail import Mail, Message
from config import Config
from models import db, User, Game, UserGame, UserPreference, Transaction, Subscription
from datetime import datetime, timedelta
import json
import logging
import traceback
import secrets
import threading
import stripe

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
app.config.from_object(Config)

# Inicializar extensiones
db.init_app(app)
mail = Mail(app)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'signin'
login_manager.login_message = 'Por favor, inicia sesión para acceder a esta página.'

# Inicializar Stripe
stripe.api_key = app.config.get('STRIPE_SECRET_KEY')

@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

# Función para inicializar base de datos (se llama después de que la app esté lista)
def init_db():
    """Inicializa la base de datos y crea tablas si no existen"""
    try:
        with app.app_context():
            logger.info("Intentando conectar a la base de datos...")
            db_url = app.config.get('SQLALCHEMY_DATABASE_URI', 'No configurada')
            # Ocultar credenciales en el log
            if '@' in db_url:
                db_url_display = db_url.split('@')[0].split('://')[0] + '://***@' + '@'.join(db_url.split('@')[1:])
            else:
                db_url_display = db_url[:50] + '...' if len(db_url) > 50 else db_url
            logger.info(f"Database URL: {db_url_display}")
            db.create_all()
            logger.info("Tablas de base de datos creadas/verificadas exitosamente")
            
            # Intentar agregar nuevas columnas si no existen (migración manual)
            try:
                from sqlalchemy import text, inspect
                inspector = inspect(db.engine)
                columns = [col['name'] for col in inspector.get_columns('users')]
                
                if 'name_change_count' not in columns:
                    logger.info("Agregando columna name_change_count a la tabla users...")
                    db.session.execute(text("ALTER TABLE users ADD COLUMN name_change_count INTEGER DEFAULT 0"))
                    db.session.commit()
                    logger.info("Columna name_change_count agregada exitosamente")
                
                if 'last_name_change_date' not in columns:
                    logger.info("Agregando columna last_name_change_date a la tabla users...")
                    db.session.execute(text("ALTER TABLE users ADD COLUMN last_name_change_date TIMESTAMP"))
                    db.session.commit()
                    logger.info("Columna last_name_change_date agregada exitosamente")
                
                # Agregar columnas de verificación de email
                if 'email_verified' not in columns:
                    logger.info("Agregando columna email_verified a la tabla users...")
                    db.session.execute(text("ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE"))
                    db.session.commit()
                    logger.info("Columna email_verified agregada exitosamente")
                
                if 'email_verification_token' not in columns:
                    logger.info("Agregando columna email_verification_token a la tabla users...")
                    db.session.execute(text("ALTER TABLE users ADD COLUMN email_verification_token VARCHAR(100)"))
                    db.session.commit()
                    logger.info("Columna email_verification_token agregada exitosamente")
                
                if 'email_verification_sent_at' not in columns:
                    logger.info("Agregando columna email_verification_sent_at a la tabla users...")
                    db.session.execute(text("ALTER TABLE users ADD COLUMN email_verification_sent_at TIMESTAMP"))
                    db.session.commit()
                    logger.info("Columna email_verification_sent_at agregada exitosamente")
                
                # Agregar columna game_url a la tabla games si no existe
                game_columns = [col['name'] for col in inspector.get_columns('games')]
                if 'game_url' not in game_columns:
                    logger.info("Agregando columna game_url a la tabla games...")
                    db.session.execute(text("ALTER TABLE games ADD COLUMN game_url VARCHAR(500)"))
                    db.session.commit()
                    logger.info("Columna game_url agregada exitosamente")
            except Exception as migration_error:
                logger.warning(f"No se pudieron agregar las nuevas columnas (puede que ya existan): {str(migration_error)}")
                db.session.rollback()
            
            # Verificar y crear tablas de pagos si no existen
            try:
                from sqlalchemy import inspect
                inspector = inspect(db.engine)
                existing_tables = inspector.get_table_names()
                
                if 'transactions' not in existing_tables:
                    logger.info("Creando tabla transactions...")
                    Transaction.__table__.create(db.engine)
                    logger.info("Tabla transactions creada exitosamente")
                
                if 'subscriptions' not in existing_tables:
                    logger.info("Creando tabla subscriptions...")
                    Subscription.__table__.create(db.engine)
                    logger.info("Tabla subscriptions creada exitosamente")
            except Exception as payment_tables_error:
                logger.warning(f"No se pudieron crear las tablas de pago (puede que ya existan): {str(payment_tables_error)}")
                db.session.rollback()
            # Crear algunos juegos de ejemplo si no existen
            if Game.query.count() == 0:
                sample_games = [
                    Game(name='Mario Kart', description='Carreras emocionantes con personajes icónicos', 
                         price=250.00, platforms='Android,iOS', category='Racing'),
                    Game(name='Roblox', description='Plataforma de creación y juego', 
                         price=150.00, platforms='Android,iOS,PC', category='Sandbox'),
                    Game(name='Call of Duty', description='Acción intensa y estratégica', 
                         price=500.00, platforms='PC,Console', category='FPS'),
                    Game(name='Space Wars', description='Engage in intergalactic battles, explore unknown galaxies', 
                         price=0.00, platforms='PC', category='Strategy'),
                    Game(name='Return of the Cars', description='Rev up your engines and race through thrilling tracks', 
                         price=0.00, platforms='PC,Console', category='Racing'),
                    Game(name='Planes of Gloria', description='Soar through the skies, engage in epic dogfights', 
                         price=0.00, platforms='PC', category='Simulation'),
                    Game(name='Earth Wars', description='Rewrite history in intense global battles', 
                         price=0.00, platforms='PC,Console', category='Strategy'),
                ]
                for game in sample_games:
                    db.session.add(game)
                db.session.commit()
                logger.info("Juegos de ejemplo creados exitosamente")
            
            # Agregar juegos chistosos si no existen
            funny_game_names = ['Flootilupis', 'Chocopops', 'SnackAttack', 'CerealKiller', 'Munchies']
            existing_funny_games = {game.name for game in Game.query.filter(Game.name.in_(funny_game_names)).all()}
            
            funny_games_data = [
                {
                    'name': 'Frootilupis Match',
                    'description': '🍩 ¡Combina 3 o más cereales del mismo color! Un juego adictivo donde los cereales vuelan y explotan con efectos increíbles. ¿Tendrás lo necesario para alcanzar el puntaje más alto?',
                    'price': 0.00,
                    'platforms': 'Android',
                    'category': 'Match-3',
                    'game_url': 'flootilupis.html'
                },
                {
                    'name': 'Chocopops Volador',
                    'description': '🍫 ¡Vuela como un chocolate loco! Toca la pantalla para hacer volar tu chocolate y esquiva los obstáculos verdes. ¿Podrás llegar más lejos que tus amigos?',
                    'price': 0.00,
                    'platforms': 'Android',
                    'category': 'Arcade',
                    'game_url': 'chocopops.html'
                },
                {
                    'name': 'SnackAttack Laberinto',
                    'description': '🍿 ¡Come todos los snacks antes de que los fantasmas te atrapen! Recolecta puntos dorados y usa los power pellets para convertirte en el rey del laberinto.',
                    'price': 0.00,
                    'platforms': 'Android',
                    'category': 'Arcade',
                    'game_url': 'snackattack.html'
                },
                {
                    'name': 'CerealKiller Connect',
                    'description': '🥣 ¡Conecta los cereales del mismo color sin que se crucen! Dibuja líneas táctiles para unir los puntos. Cada nivel es más difícil que el anterior. ¿Podrás con el desafío?',
                    'price': 0.00,
                    'platforms': 'Android',
                    'category': 'Puzzle',
                    'game_url': 'cerealkiller.html'
                },
                {
                    'name': 'Munchies Memory',
                    'description': '🧠 ¡Encuentra todos los pares de snacks antes de que se acabe el tiempo! Entrena tu memoria con este juego relajante lleno de deliciosos snacks. ¿Tienes buena memoria?',
                    'price': 0.00,
                    'platforms': 'Android',
                    'category': 'Memory',
                    'game_url': 'munchies.html'
                }
            ]
            
            games_added = 0
            games_updated = 0
            for game_data in funny_games_data:
                existing_game = Game.query.filter_by(name=game_data['name']).first()
                
                # Buscar por nombre antiguo si existe (para migración)
                if not existing_game:
                    old_names = {
                        'Frootilupis Match': 'Flootilupis',
                        'Chocopops Volador': 'Chocopops',
                        'SnackAttack Laberinto': 'SnackAttack',
                        'CerealKiller Connect': 'CerealKiller',
                        'Munchies Memory': 'Munchies'
                    }
                    old_name = old_names.get(game_data['name'])
                    if old_name:
                        existing_game = Game.query.filter_by(name=old_name).first()
                        if existing_game:
                            existing_game.name = game_data['name']  # Actualizar nombre
                
                if not existing_game:
                    # Crear nuevo juego
                    game = Game(
                        name=game_data['name'],
                        description=game_data['description'],
                        price=game_data['price'],
                        platforms=game_data['platforms'],
                        category=game_data['category'],
                        game_url=game_data['game_url']
                    )
                    db.session.add(game)
                    games_added += 1
                    logger.info(f"Agregando juego: {game_data['name']}")
                else:
                    # Actualizar juego existente (nombre, descripción, URL, etc.)
                    needs_update = False
                    if existing_game.name != game_data['name']:
                        existing_game.name = game_data['name']
                        needs_update = True
                    if existing_game.description != game_data['description']:
                        existing_game.description = game_data['description']
                        needs_update = True
                    if existing_game.game_url != game_data['game_url']:
                        existing_game.game_url = game_data['game_url']
                        needs_update = True
                    if existing_game.platforms != game_data['platforms']:
                        existing_game.platforms = game_data['platforms']
                        needs_update = True
                    if existing_game.price != game_data['price']:
                        existing_game.price = game_data['price']
                        needs_update = True
                    if existing_game.category != game_data['category']:
                        existing_game.category = game_data['category']
                        needs_update = True
                    
                    if needs_update:
                        games_updated += 1
                        logger.info(f"Actualizando juego: {game_data['name']}")
            
            if games_added > 0 or games_updated > 0:
                db.session.commit()
                if games_added > 0:
                    logger.info(f"Se agregaron {games_added} juegos chistosos exitosamente")
                if games_updated > 0:
                    logger.info(f"Se actualizaron {games_updated} juegos chistosos exitosamente")
            else:
                db.session.commit()
                logger.info("Juegos chistosos ya existen y están actualizados")
    except Exception as e:
        logger.error(f"Error al inicializar base de datos: {str(e)}")
        logger.error(traceback.format_exc())
        # No lanzar el error, permitir que la app inicie aunque falle la BD
        # (útil para debugging)

# Inicializar base de datos al importar el módulo
init_db()

# ==================== RUTAS PÚBLICAS ====================

@app.route('/')
def index():
    # Limpiar intención de suscribirse si el usuario viene a la página principal
    # Esto evita redirecciones no deseadas
    if 'intent_to_subscribe' in session:
        session.pop('intent_to_subscribe', None)
    return render_template('index.html')

@app.route('/beneficios')
def benefits():
    # Guardar en sesión que el usuario quiere suscribirse
    session['intent_to_subscribe'] = True
    return render_template('benefits.html')

@app.route('/signin')
def signin():
    # SIEMPRE mostrar el formulario de login
    # NO hacer ninguna redirección automática
    # El usuario DEBE completar el formulario primero
    return render_template('signin.html')

@app.route('/login')
def login():
    # SIEMPRE mostrar el formulario de registro
    # NO hacer ninguna redirección automática
    # El usuario DEBE completar el formulario primero
    return render_template('login.html')

# ==================== RUTAS PROTEGIDAS ====================

@app.route('/welcome')
@login_required
def welcome():
    # Verificar si el usuario tiene una suscripción activa
    active_subscription = Subscription.query.filter_by(
        user_id=current_user.id,
        status='active'
    ).first()
    
    has_subscription = active_subscription is not None
    
    return render_template('welcome.html', has_subscription=has_subscription)

@app.route('/profile')
@login_required
def profile():
    return render_template('profile.html')

@app.route('/settings')
@login_required
def settings():
    return render_template('settings.html')

# ==================== FUNCIONES DE EMAIL ====================

def send_verification_email(user, token):
    """Envía email de verificación al usuario (ejecuta en thread separado)"""
    def _send_email():
        """Función interna que envía el email en un thread separado"""
        try:
            logger.info("=" * 50)
            logger.info("INICIANDO ENVÍO DE EMAIL EN THREAD")
            logger.info("=" * 50)
            
            # Verificar configuración de email
            mail_server = app.config.get('MAIL_SERVER')
            mail_port = app.config.get('MAIL_PORT')
            mail_username = app.config.get('MAIL_USERNAME')
            mail_password = app.config.get('MAIL_PASSWORD')
            mail_use_tls = app.config.get('MAIL_USE_TLS')
            mail_default_sender = app.config.get('MAIL_DEFAULT_SENDER')
            
            logger.info(f"MAIL_SERVER: {mail_server}")
            logger.info(f"MAIL_PORT: {mail_port}")
            logger.info(f"MAIL_USERNAME: {mail_username}")
            logger.info(f"MAIL_PASSWORD configurado: {'Sí' if mail_password else 'No'}")
            logger.info(f"MAIL_USE_TLS: {mail_use_tls}")
            logger.info(f"MAIL_DEFAULT_SENDER: {mail_default_sender}")
            
            if not mail_server or not mail_username or not mail_password:
                logger.error("❌ Configuración de email incompleta!")
                logger.error(f"MAIL_SERVER: {mail_server}")
                logger.error(f"MAIL_USERNAME: {mail_username}")
                logger.error(f"MAIL_PASSWORD: {'Configurado' if mail_password else 'NO CONFIGURADO'}")
                return False
            
            logger.info(f"✅ Configuración de email OK. Intentando enviar a: {user.email}")
            
            verification_url = f"{app.config.get('APP_URL', 'https://pixelpick-akp2.onrender.com')}/verify-email?token={token}"
            
            msg = Message(
                subject='Verifica tu correo electrónico - PixelPick',
                recipients=[user.email],
                sender=mail_default_sender,
                html=f"""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {{ font-family: Arial, sans-serif; line-height: 1.6; color: #333; }}
                        .container {{ max-width: 600px; margin: 0 auto; padding: 20px; }}
                        .header {{ background: linear-gradient(135deg, #00d4ff 0%, #5b86e5 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }}
                        .header h1 {{ color: white; margin: 0; }}
                        .content {{ background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }}
                        .button {{ display: inline-block; padding: 12px 30px; background: linear-gradient(135deg, #00d4ff 0%, #5b86e5 100%); color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }}
                        .footer {{ text-align: center; margin-top: 20px; color: #666; font-size: 12px; }}
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✨ PixelPick ✨</h1>
                        </div>
                        <div class="content">
                            <h2>¡Bienvenido a PixelPick, {user.first_name}!</h2>
                            <p>Gracias por registrarte. Para completar tu registro, por favor verifica tu correo electrónico haciendo clic en el botón de abajo:</p>
                            <div style="text-align: center;">
                                <a href="{verification_url}" class="button">Verificar Mi Correo</a>
                            </div>
                            <p>O copia y pega este enlace en tu navegador:</p>
                            <p style="word-break: break-all; color: #00d4ff;">{verification_url}</p>
                            <p>Este enlace expirará en 24 horas.</p>
                            <p>Si no creaste una cuenta en PixelPick, puedes ignorar este correo.</p>
                        </div>
                        <div class="footer">
                            <p>© 2025 PixelPick. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
            )
            
            logger.info(f"📧 Mensaje creado. Intentando conectar a SMTP...")
            
            # Enviar email con app context
            with app.app_context():
                try:
                    mail.send(msg)
                    logger.info(f"✅ Email de verificación enviado exitosamente a: {user.email}")
                    return True
                except Exception as smtp_error:
                    logger.error(f"❌ Error SMTP al enviar email: {str(smtp_error)}")
                    logger.error(f"Tipo de error: {type(smtp_error).__name__}")
                    raise
            
        except Exception as e:
            logger.error("=" * 50)
            logger.error("ERROR AL ENVIAR EMAIL")
            logger.error("=" * 50)
            logger.error(f"Error: {str(e)}")
            logger.error(f"Tipo: {type(e).__name__}")
            logger.error(traceback.format_exc())
            return False
    
    # Ejecutar en thread separado para no bloquear la respuesta
    logger.info(f"🚀 Iniciando thread para enviar email a: {user.email}")
    thread = threading.Thread(target=_send_email, name=f"EmailThread-{user.email}")
    thread.daemon = True
    thread.start()
    logger.info(f"✅ Thread de envío de email iniciado para: {user.email}")
    return True

# ==================== API RUTAS - AUTENTICACIÓN ====================

@app.route('/api/register', methods=['POST'])
def register():
    """Registro de nuevo usuario"""
    try:
        logger.info("Request recibido en /api/register")
        logger.info(f"Headers: {dict(request.headers)}")
        data = request.get_json()
        logger.info(f"Datos recibidos: {data}")
        
        # Validar datos
        if not data:
            logger.warning("No se recibieron datos en el request")
            return jsonify({'error': 'No se recibieron datos'}), 400
        
        first_name = data.get('firstName', '').strip()
        last_name = data.get('lastName', '').strip()
        email = data.get('email', '').strip().lower()
        password = data.get('password', '')
        terms = data.get('terms', False)
        
        # Validaciones
        if not all([first_name, last_name, email, password]):
            return jsonify({'error': 'Todos los campos son requeridos'}), 400
        
        if not terms:
            return jsonify({'error': 'Debes aceptar los términos de servicio'}), 400
        
        if len(password) < 8:
            return jsonify({'error': 'La contraseña debe tener al menos 8 caracteres'}), 400
        
        # Verificar si el email ya existe
        if User.query.filter_by(email=email).first():
            return jsonify({'error': 'Este correo electrónico ya está registrado'}), 400
        
        # Crear nuevo usuario
        logger.info(f"Creando nuevo usuario: {first_name} {last_name} ({email})")
        user = User(
            first_name=first_name,
            last_name=last_name,
            email=email
        )
        user.set_password(password)
        
        # Agregar usuario a la sesión de base de datos
        logger.info("Agregando usuario a la sesión de base de datos...")
        db.session.add(user)
        
        # Guardar en la base de datos
        logger.info("Guardando usuario en la base de datos (commit)...")
        try:
            db.session.commit()
            logger.info(f"✅ Usuario guardado exitosamente en la base de datos. ID: {user.id}, Email: {email}")
            
            # Verificar que el usuario se guardó correctamente
            saved_user = User.query.filter_by(email=email).first()
            if saved_user:
                logger.info(f"✅ Verificación: Usuario encontrado en BD con ID {saved_user.id}")
            else:
                logger.error(f"❌ ERROR: Usuario no encontrado en BD después del commit!")
        except Exception as db_error:
            logger.error(f"❌ Error al hacer commit en la base de datos: {str(db_error)}")
            db.session.rollback()
            raise
        
        # Iniciar sesión automáticamente después del registro
        login_user(user, remember=True)
        logger.info(f"✅ Sesión iniciada para usuario: {email}")
        
        # Verificar si el usuario tenía intención de suscribirse
        intent_to_subscribe = session.get('intent_to_subscribe', False)
        if intent_to_subscribe:
            session.pop('intent_to_subscribe', None)  # Limpiar la sesión
            logger.info("Usuario tiene intención de suscribirse, redirigirá al checkout")
        
        return jsonify({
            'success': True,
            'message': 'Usuario registrado exitosamente',
            'user': user.to_dict(),
            'redirect_to_checkout': intent_to_subscribe
        }), 201
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al registrar usuario: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({'error': f'Error al registrar usuario: {str(e)}'}), 500

@app.route('/api/login', methods=['POST'])
def login_api():
    """Inicio de sesión"""
    try:
        logger.info("Request recibido en /api/login")
        data = request.get_json()
        logger.info(f"Datos recibidos: email={data.get('email') if data else 'None'}")
        
        if not data:
            return jsonify({'error': 'No se recibieron datos'}), 400
        
        email = data.get('email', '').strip().lower()
        password = data.get('password', '')
        
        if not email or not password:
            return jsonify({'error': 'Email y contraseña son requeridos'}), 400
        
        # Buscar usuario
        user = User.query.filter_by(email=email).first()
        
        if not user or not user.check_password(password):
            return jsonify({'error': 'Email o contraseña incorrectos'}), 401
        
        # Iniciar sesión
        login_user(user, remember=True)
        
        # Verificar si el usuario tenía intención de suscribirse
        intent_to_subscribe = session.get('intent_to_subscribe', False)
        if intent_to_subscribe:
            session.pop('intent_to_subscribe', None)  # Limpiar la sesión
        
        return jsonify({
            'success': True,
            'message': 'Inicio de sesión exitoso',
            'user': user.to_dict(),
            'redirect_to_checkout': intent_to_subscribe
        }), 200
        
    except Exception as e:
        logger.error(f"Error al iniciar sesión: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({'error': f'Error al iniciar sesión: {str(e)}'}), 500

@app.route('/api/logout', methods=['POST'])
@login_required
def logout_api():
    """Cerrar sesión"""
    try:
        logout_user()
        return jsonify({'success': True, 'message': 'Sesión cerrada exitosamente'}), 200
    except Exception as e:
        return jsonify({'error': f'Error al cerrar sesión: {str(e)}'}), 500

@app.route('/api/user', methods=['GET'])
@login_required
def get_current_user():
    """Obtener información del usuario actual"""
    try:
        return jsonify({
            'success': True,
            'user': current_user.to_dict()
        }), 200
    except Exception as e:
        return jsonify({'error': f'Error al obtener usuario: {str(e)}'}), 500

# ==================== API RUTAS - PERFIL ====================

@app.route('/api/profile', methods=['GET'])
@login_required
def get_profile():
    """Obtener perfil completo del usuario"""
    try:
        user = current_user
        
        # Obtener juegos del usuario (ordenados por última vez jugado)
        user_games = UserGame.query.filter_by(user_id=user.id).order_by(UserGame.last_played.desc()).limit(10).all()
        games_data = [ug.to_dict() for ug in user_games]
        
        # Estadísticas - contar solo juegos únicos por status
        # Completados: juegos únicos con status 'completed'
        completed_games = db.session.query(UserGame.game_id).filter_by(
            user_id=user.id, 
            status='completed'
        ).distinct().all()
        completed_count = len(completed_games)
        
        # Jugando: juegos únicos con status 'playing'
        playing_games = db.session.query(UserGame.game_id).filter_by(
            user_id=user.id, 
            status='playing'
        ).distinct().all()
        playing_count = len(playing_games)
        
        return jsonify({
            'success': True,
            'user': user.to_dict(),
            'games': games_data,
            'stats': {
                'completed': completed_count,
                'playing': playing_count
            }
        }), 200
        
    except Exception as e:
        return jsonify({'error': f'Error al obtener perfil: {str(e)}'}), 500

@app.route('/api/profile/update', methods=['PUT'])
@login_required
def update_profile():
    """Actualizar perfil del usuario con límite de 3 cambios"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No se recibieron datos'}), 400
        
        user = current_user
        
        # Verificar si el nombre o apellido realmente cambió
        first_name_changed = 'firstName' in data and data['firstName'].strip() != user.first_name
        last_name_changed = 'lastName' in data and data['lastName'].strip() != user.last_name
        
        # Si no hay cambios, retornar éxito sin hacer nada
        if not first_name_changed and not last_name_changed:
            return jsonify({
                'success': True,
                'message': 'No se realizaron cambios',
                'user': user.to_dict(),
                'changes_remaining': 3 - (getattr(user, 'name_change_count', None) or 0)
            }), 200
        
        # Si hay cambios en nombre o apellido, verificar si puede cambiar
        if first_name_changed or last_name_changed:
            can_change, error_message = user.can_change_name()
            if not can_change:
                return jsonify({
                    'success': False,
                    'error': error_message,
                    'changes_remaining': 0
                }), 403
        
        # Validar que los campos no estén vacíos
        new_first_name = data.get('firstName', '').strip() if 'firstName' in data else user.first_name
        new_last_name = data.get('lastName', '').strip() if 'lastName' in data else user.last_name
        
        if not new_first_name or not new_last_name:
            return jsonify({'error': 'El nombre y apellido son requeridos'}), 400
        
        # Actualizar campos
        user.first_name = new_first_name
        user.last_name = new_last_name
        
        # Si cambió el nombre o apellido, incrementar contador y actualizar fecha
        if first_name_changed or last_name_changed:
            # Usar getattr para manejar columnas que pueden no existir aún
            current_count = getattr(user, 'name_change_count', None) or 0
            if hasattr(user, 'name_change_count'):
                user.name_change_count = current_count + 1
                user.last_name_change_date = datetime.utcnow()
            else:
                # Si las columnas no existen, intentar agregarlas
                logger.warning("Las columnas de cambio de nombre no existen, intentando agregarlas...")
                try:
                    from sqlalchemy import text, inspect
                    inspector = inspect(db.engine)
                    columns = [col['name'] for col in inspector.get_columns('users')]
                    
                    if 'name_change_count' not in columns:
                        db.session.execute(text("ALTER TABLE users ADD COLUMN name_change_count INTEGER DEFAULT 0"))
                    if 'last_name_change_date' not in columns:
                        db.session.execute(text("ALTER TABLE users ADD COLUMN last_name_change_date TIMESTAMP"))
                    
                    db.session.commit()
                    # Recargar el usuario
                    db.session.refresh(user)
                    user.name_change_count = 1
                    user.last_name_change_date = datetime.utcnow()
                except Exception as e:
                    logger.error(f"Error al agregar columnas: {str(e)}")
                    db.session.rollback()
                    # Continuar sin las columnas (modo compatible)
        
        user.updated_at = datetime.utcnow()
        db.session.commit()
        
        name_change_count = getattr(user, 'name_change_count', None) or 0
        changes_remaining = max(0, 3 - name_change_count)
        
        logger.info(f"Usuario {user.email} actualizó su perfil. Cambios restantes: {changes_remaining}")
        
        return jsonify({
            'success': True,
            'message': 'Nombre y apellido actualizados correctamente',
            'user': user.to_dict(),
            'changes_remaining': changes_remaining
        }), 200
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al actualizar perfil: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({'error': f'Error al actualizar perfil: {str(e)}'}), 500

@app.route('/api/profile/delete', methods=['DELETE'])
@login_required
def delete_account():
    """Eliminar cuenta del usuario"""
    try:
        user = current_user
        user_id = user.id
        
        # Eliminar usuario (las relaciones se eliminan en cascada)
        db.session.delete(user)
        db.session.commit()
        
        logout_user()
        
        return jsonify({
            'success': True,
            'message': 'Cuenta eliminada exitosamente'
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': f'Error al eliminar cuenta: {str(e)}'}), 500

# ==================== API RUTAS - JUEGOS ====================

@app.route('/api/games', methods=['GET'])
@login_required
def get_games():
    """Obtener lista de juegos"""
    try:
        games = Game.query.all()
        games_data = [game.to_dict() for game in games]
        
        return jsonify({
            'success': True,
            'games': games_data
        }), 200
        
    except Exception as e:
        return jsonify({'error': f'Error al obtener juegos: {str(e)}'}), 500

@app.route('/api/games/recommendations', methods=['GET'])
@login_required
def get_recommendations():
    """Obtener recomendaciones de juegos basadas en los juegos que el usuario ha jugado"""
    try:
        user = current_user
        
        # Obtener juegos que el usuario ha jugado
        user_games = UserGame.query.filter_by(user_id=user.id).all()
        played_game_ids = [ug.game_id for ug in user_games]
        
        # Obtener todos los juegos del catálogo (los 5 juegos chistosos)
        funny_game_names = ['Frootilupis Match', 'Chocopops Volador', 'SnackAttack Laberinto', 
                           'CerealKiller Connect', 'Munchies Memory', 'Flootilupis', 'Chocopops', 
                           'SnackAttack', 'CerealKiller', 'Munchies']
        all_games = Game.query.filter(Game.name.in_(funny_game_names)).all()
        
        # Analizar categorías de los juegos que ha jugado (si hay)
        played_categories = {}
        if played_game_ids:
            played_games = Game.query.filter(Game.id.in_(played_game_ids)).all()
            for game in played_games:
                if game.category:
                    category = game.category.lower()
                    played_categories[category] = played_categories.get(category, 0) + 1
        
        # Calcular puntuación de recomendación para TODOS los juegos
        # Las recomendaciones siempre deben estar visibles y cambiar dinámicamente
        game_scores = []
        for game in all_games:
            score = 0
            
            # Si el usuario NO ha jugado este juego, darle más puntos
            if game.id not in played_game_ids:
                score += 20  # Bonus por no haberlo jugado
            
            # Puntos por categoría similar (si ha jugado juegos similares)
            if game.category and game.category.lower() in played_categories:
                score += played_categories[game.category.lower()] * 5
            
            # Puntos por plataforma (si coincide)
            if game.platforms:
                platforms = game.platforms.split(',') if isinstance(game.platforms, str) else game.platforms
                if 'Android' in platforms:
                    score += 3
            
            # Bonus si es gratis
            if game.price == 0.00:
                score += 2
            
            # Si el usuario ya lo está jugando, darle menos puntos pero aún incluirlo
            # Esto permite que las recomendaciones cambien dinámicamente
            if game.id in played_game_ids:
                score -= 10  # Penalización por ya haberlo jugado
            
            game_scores.append((game, score))
        
        # Ordenar por puntuación y tomar los mejores 3
        game_scores.sort(key=lambda x: x[1], reverse=True)
        recommended_games_with_scores = game_scores[:3]
        
        # Asegurarse de que siempre haya 3 recomendaciones
        if len(recommended_games_with_scores) < 3:
            recommended_ids = [gs[0].id for gs in recommended_games_with_scores]
            additional_games = [g for g in all_games if g.id not in recommended_ids]
            for game in additional_games[:3 - len(recommended_games_with_scores)]:
                recommended_games_with_scores.append((game, 0))
        
        # Mapeo de categorías en inglés a español
        category_translations = {
            'arcade': 'acción rápida',
            'match-3': 'puzzle de combinación',
            'puzzle': 'puzzle',
            'memory': 'memoria',
            'strategy': 'estrategia',
            'racing': 'carreras',
            'fps': 'disparos',
            'sandbox': 'mundo abierto',
            'simulation': 'simulación'
        }
        
        # Generar razones de recomendación para cada juego
        games_data = []
        for game, score in recommended_games_with_scores[:3]:
            game_dict = game.to_dict()
            
            # Generar razón de recomendación basada en el análisis
            reason = ""
            if game.id not in played_game_ids:
                # Si el usuario no ha jugado este juego
                if game.category and game.category.lower() in played_categories:
                    # Si ha jugado juegos de la misma categoría
                    category_es = category_translations.get(game.category.lower(), game.category.lower())
                    reason = f"Te gustan los juegos de {category_es}"
                elif game.category:
                    # Si tiene categoría pero no ha jugado juegos similares
                    category_es = category_translations.get(game.category.lower(), game.category.lower())
                    reason = f"Perfecto si te gustan los juegos de {category_es}"
                else:
                    reason = "Nuevo juego perfecto para ti"
            else:
                # Si el usuario ya ha jugado este juego
                if game.category:
                    category_es = category_translations.get(game.category.lower(), game.category.lower())
                    reason = f"Basado en tu interés por juegos de {category_es}"
                else:
                    reason = "Recomendado según tus preferencias"
            
            game_dict['recommendation_reason'] = reason
            games_data.append(game_dict)
        
        return jsonify({
            'success': True,
            'recommendations': games_data
        }), 200
        
    except Exception as e:
        logger.error(f"Error al obtener recomendaciones: {str(e)}")
        logger.error(traceback.format_exc())
        # En caso de error, devolver juegos por defecto
        try:
            funny_game_names = ['Frootilupis Match', 'Chocopops Volador', 'SnackAttack Laberinto', 
                               'CerealKiller Connect', 'Munchies Memory']
            default_games = Game.query.filter(Game.name.in_(funny_game_names)).limit(3).all()
            games_data = [game.to_dict() for game in default_games]
            return jsonify({
                'success': True,
                'recommendations': games_data
            }), 200
        except:
            return jsonify({'error': f'Error al obtener recomendaciones: {str(e)}'}), 500

@app.route('/api/user/games', methods=['GET'])
@login_required
def get_user_games():
    """Obtener juegos del usuario"""
    try:
        user_games = UserGame.query.filter_by(user_id=current_user.id).all()
        games_data = [ug.to_dict() for ug in user_games]
        
        return jsonify({
            'success': True,
            'games': games_data
        }), 200
        
    except Exception as e:
        return jsonify({'error': f'Error al obtener juegos: {str(e)}'}), 500

@app.route('/api/user/games', methods=['POST'])
@login_required
def add_user_game():
    """Agregar o actualizar juego del usuario"""
    try:
        data = request.get_json()
        
        if not data or 'game_id' not in data:
            return jsonify({'error': 'game_id es requerido'}), 400
        
        game_id = data['game_id']
        status = data.get('status', 'playing')
        
        # Verificar que el juego existe
        game = Game.query.get(game_id)
        if not game:
            return jsonify({'error': 'Juego no encontrado'}), 404
        
        # Verificar si ya existe
        existing = UserGame.query.filter_by(user_id=current_user.id, game_id=game_id).first()
        is_new_game = False
        
        if existing:
            # Actualizar estado y fecha (no es un juego nuevo, solo se actualiza)
            # Si el juego estaba completado y ahora se vuelve a jugar, cambiar a 'playing'
            existing.status = status
            existing.last_played = datetime.utcnow()
        else:
            # Crear nueva relación (es un juego nuevo)
            is_new_game = True
            user_game = UserGame(
                user_id=current_user.id,
                game_id=game_id,
                status=status
            )
            db.session.add(user_game)
        
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': 'Juego agregado exitosamente',
            'is_new': is_new_game  # Indicar si es un juego nuevo
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': f'Error al agregar juego: {str(e)}'}), 500

@app.route('/api/user/games/<int:game_id>/complete', methods=['POST'])
@login_required
def complete_game(game_id):
    """Marcar juego como completado"""
    try:
        # Verificar que el juego existe
        game = Game.query.get(game_id)
        if not game:
            return jsonify({'error': 'Juego no encontrado'}), 404
        
        # Buscar o crear relación usuario-juego
        user_game = UserGame.query.filter_by(user_id=current_user.id, game_id=game_id).first()
        if user_game:
            # Actualizar a completado
            user_game.status = 'completed'
            user_game.last_played = datetime.utcnow()
        else:
            # Crear nueva relación como completado
            user_game = UserGame(
                user_id=current_user.id,
                game_id=game_id,
                status='completed'
            )
            db.session.add(user_game)
        
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': 'Juego marcado como completado'
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': f'Error al completar juego: {str(e)}'}), 500

# ==================== RUTAS DE VERIFICACIÓN DE EMAIL ====================

@app.route('/verify-email')
def verify_email():
    """Página para verificar email con token"""
    token = request.args.get('token')
    
    if not token:
        return render_template('email_verification.html', 
                             success=False, 
                             message='Token de verificación no proporcionado')
    
    # Buscar usuario con el token
    user = User.query.filter_by(email_verification_token=token).first()
    
    if not user:
        return render_template('email_verification.html', 
                             success=False, 
                             message='Token de verificación inválido o expirado')
    
    # Verificar si el token no ha expirado (24 horas)
    if user.email_verification_sent_at:
        time_diff = datetime.utcnow() - user.email_verification_sent_at
        if time_diff > timedelta(hours=24):
            return render_template('email_verification.html', 
                                 success=False, 
                                 message='El token de verificación ha expirado. Por favor, solicita uno nuevo.')
    
    # Verificar el email
    user.email_verified = True
    user.email_verification_token = None  # Eliminar el token usado
    db.session.commit()
    
    logger.info(f"Email verificado exitosamente para: {user.email}")
    
    return render_template('email_verification.html', 
                         success=True, 
                         message='¡Tu correo electrónico ha sido verificado exitosamente! Ya puedes iniciar sesión.')

@app.route('/api/verify-email', methods=['POST'])
def verify_email_api():
    """API para verificar email con token"""
    try:
        data = request.get_json()
        token = data.get('token') if data else request.args.get('token')
        
        if not token:
            return jsonify({'error': 'Token de verificación no proporcionado'}), 400
        
        # Buscar usuario con el token
        user = User.query.filter_by(email_verification_token=token).first()
        
        if not user:
            return jsonify({'error': 'Token de verificación inválido o expirado'}), 400
        
        # Verificar si el token no ha expirado (24 horas)
        if user.email_verification_sent_at:
            time_diff = datetime.utcnow() - user.email_verification_sent_at
            if time_diff > timedelta(hours=24):
                return jsonify({'error': 'El token de verificación ha expirado. Por favor, solicita uno nuevo.'}), 400
        
        # Verificar el email
        user.email_verified = True
        user.email_verification_token = None
        db.session.commit()
        
        logger.info(f"Email verificado exitosamente para: {user.email}")
        
        return jsonify({
            'success': True,
            'message': 'Correo electrónico verificado exitosamente'
        }), 200
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al verificar email: {str(e)}")
        return jsonify({'error': f'Error al verificar email: {str(e)}'}), 500

@app.route('/api/resend-verification', methods=['POST'])
def resend_verification():
    """Reenviar email de verificación"""
    try:
        data = request.get_json()
        email = data.get('email', '').strip().lower() if data else None
        
        if not email:
            return jsonify({'error': 'Email es requerido'}), 400
        
        # Buscar usuario
        user = User.query.filter_by(email=email).first()
        
        if not user:
            # Por seguridad, no revelar si el email existe o no
            return jsonify({
                'success': True,
                'message': 'Si el email existe y no está verificado, se enviará un nuevo correo de verificación.'
            }), 200
        
        # Verificar si ya está verificado
        email_verified = getattr(user, 'email_verified', False)
        if email_verified:
            return jsonify({'error': 'Este correo electrónico ya está verificado'}), 400
        
        # Verificar límite de reenvíos (máximo 1 por hora)
        if user.email_verification_sent_at:
            time_diff = datetime.utcnow() - user.email_verification_sent_at
            if time_diff < timedelta(hours=1):
                minutes_remaining = int((timedelta(hours=1) - time_diff).total_seconds() / 60)
                return jsonify({
                    'error': f'Debes esperar {minutes_remaining} minutos antes de solicitar otro correo de verificación'
                }), 429
        
        # Generar nuevo token
        verification_token = secrets.token_urlsafe(32)
        user.email_verification_token = verification_token
        user.email_verification_sent_at = datetime.utcnow()
        db.session.commit()
        
        # Enviar email
        try:
            send_verification_email(user, verification_token)
            logger.info(f"Email de verificación reenviado a: {email}")
            return jsonify({
                'success': True,
                'message': 'Correo de verificación reenviado exitosamente. Revisa tu bandeja de entrada.'
            }), 200
        except Exception as email_error:
            logger.error(f"Error al reenviar email: {str(email_error)}")
            return jsonify({
                'error': 'Error al enviar el correo de verificación. Por favor, intenta más tarde.'
            }), 500
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al reenviar verificación: {str(e)}")
        return jsonify({'error': f'Error al procesar la solicitud: {str(e)}'}), 500

# Ruta de diagnóstico
@app.route('/api/health', methods=['GET'])
def health_check():
    """Endpoint para verificar el estado de la aplicación y base de datos"""
    try:
        # Verificar conexión a base de datos
        db.session.execute(db.text('SELECT 1'))
        db_status = 'connected'
        db_url = app.config.get('SQLALCHEMY_DATABASE_URI', 'Not configured')
        # Ocultar credenciales en el log
        if '@' in db_url:
            db_url_display = db_url.split('@')[0].split('://')[0] + '://***@' + '@'.join(db_url.split('@')[1:])
        else:
            db_url_display = db_url
    except Exception as e:
        db_status = f'error: {str(e)}'
        db_url_display = app.config.get('SQLALCHEMY_DATABASE_URI', 'Not configured')
    
    # Verificar configuración de email
    mail_config = {
        'MAIL_SERVER': app.config.get('MAIL_SERVER'),
        'MAIL_PORT': app.config.get('MAIL_PORT'),
        'MAIL_USERNAME': app.config.get('MAIL_USERNAME'),
        'MAIL_PASSWORD': 'Configurado' if app.config.get('MAIL_PASSWORD') else 'NO CONFIGURADO',
        'MAIL_USE_TLS': app.config.get('MAIL_USE_TLS'),
        'MAIL_DEFAULT_SENDER': app.config.get('MAIL_DEFAULT_SENDER'),
        'APP_URL': app.config.get('APP_URL')
    }
    
    return jsonify({
        'status': 'ok',
        'database': db_status,
        'database_url': db_url_display,
        'secret_key_configured': bool(app.config.get('SECRET_KEY') and app.config.get('SECRET_KEY') != 'dev-secret-key-change-in-production'),
        'email_config': mail_config
    }), 200

@app.route('/api/test-email', methods=['POST'])
def test_email():
    """Endpoint para probar el envío de email (síncrono para diagnóstico)"""
    try:
        data = request.get_json()
        test_email_address = data.get('email', '').strip().lower() if data else None
        
        if not test_email_address:
            return jsonify({'error': 'Email es requerido'}), 400
        
        logger.info("=" * 50)
        logger.info("PRUEBA DE ENVÍO DE EMAIL")
        logger.info("=" * 50)
        
        # Verificar configuración
        mail_server = app.config.get('MAIL_SERVER')
        mail_port = app.config.get('MAIL_PORT')
        mail_username = app.config.get('MAIL_USERNAME')
        mail_password = app.config.get('MAIL_PASSWORD')
        mail_use_tls = app.config.get('MAIL_USE_TLS')
        mail_default_sender = app.config.get('MAIL_DEFAULT_SENDER')
        
        logger.info(f"MAIL_SERVER: {mail_server}")
        logger.info(f"MAIL_PORT: {mail_port}")
        logger.info(f"MAIL_USERNAME: {mail_username}")
        logger.info(f"MAIL_PASSWORD: {'Configurado' if mail_password else 'NO CONFIGURADO'}")
        logger.info(f"MAIL_USE_TLS: {mail_use_tls}")
        logger.info(f"MAIL_DEFAULT_SENDER: {mail_default_sender}")
        
        if not mail_server or not mail_username or not mail_password:
            error_msg = "Configuración de email incompleta"
            logger.error(error_msg)
            return jsonify({'error': error_msg, 'config': {
                'MAIL_SERVER': mail_server,
                'MAIL_USERNAME': mail_username,
                'MAIL_PASSWORD': 'Configurado' if mail_password else 'NO CONFIGURADO'
            }}), 400
        
        # Crear mensaje de prueba
        msg = Message(
            subject='Prueba de Email - PixelPick',
            recipients=[test_email_address],
            sender=mail_default_sender,
            html="""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #00d4ff 0%, #5b86e5 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .header h1 { color: white; margin: 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✨ PixelPick ✨</h1>
                    </div>
                    <div class="content">
                        <h2>¡Email de Prueba!</h2>
                        <p>Si recibes este correo, significa que la configuración de email está funcionando correctamente.</p>
                        <p>Fecha y hora: {}</p>
                    </div>
                </div>
            </body>
            </html>
            """.format(datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S UTC'))
        )
        
        logger.info(f"Intentando enviar email de prueba a: {test_email_address}")
        
        # Intentar enviar (síncrono para ver errores)
        try:
            mail.send(msg)
            logger.info(f"✅ Email de prueba enviado exitosamente a: {test_email_address}")
            return jsonify({
                'success': True,
                'message': f'Email de prueba enviado exitosamente a {test_email_address}. Revisa tu bandeja de entrada.'
            }), 200
        except Exception as smtp_error:
            error_details = {
                'error': str(smtp_error),
                'error_type': type(smtp_error).__name__,
                'config': {
                    'MAIL_SERVER': mail_server,
                    'MAIL_PORT': mail_port,
                    'MAIL_USERNAME': mail_username,
                    'MAIL_USE_TLS': mail_use_tls,
                    'MAIL_DEFAULT_SENDER': mail_default_sender
                }
            }
            logger.error(f"❌ Error al enviar email de prueba: {str(smtp_error)}")
            logger.error(traceback.format_exc())
            return jsonify(error_details), 500
            
    except Exception as e:
        logger.error(f"Error en test-email: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({'error': f'Error al procesar la solicitud: {str(e)}'}), 500

# ==================== RUTAS DE PAGO - STRIPE ====================

@app.route('/checkout')
@login_required
def checkout():
    """Página de checkout para el plan Pixelie"""
    stripe_public_key = app.config.get('STRIPE_PUBLIC_KEY')
    if not stripe_public_key:
        logger.warning("STRIPE_PUBLIC_KEY no configurada")
    
    plan_price = app.config.get('PIXELIE_PLAN_PRICE', 100.00)
    plan_currency = app.config.get('PIXELIE_PLAN_CURRENCY', 'mxn')
    
    return render_template('checkout.html', 
                         stripe_public_key=stripe_public_key,
                         plan_price=plan_price,
                         plan_currency=plan_currency.upper())

@app.route('/api/subscription/status', methods=['GET'])
@login_required
def get_subscription_status():
    """Obtener el estado de la suscripción del usuario actual"""
    try:
        # Verificar si el usuario tiene una suscripción activa
        active_subscription = Subscription.query.filter_by(
            user_id=current_user.id,
            status='active'
        ).first()
        
        has_subscription = active_subscription is not None
        
        subscription_data = None
        if active_subscription:
            subscription_data = {
                'id': active_subscription.id,
                'user_id': active_subscription.user_id,
                'plan_type': active_subscription.plan_type,
                'amount': float(active_subscription.amount) if active_subscription.amount else 0.0,
                'currency': active_subscription.currency,
                'status': active_subscription.status,
                'current_period_start': active_subscription.current_period_start.isoformat() if active_subscription.current_period_start else None,
                'current_period_end': active_subscription.current_period_end.isoformat() if active_subscription.current_period_end else None
            }
        
        return jsonify({
            'success': True,
            'user': {
                'has_subscription': has_subscription,
                'subscription': subscription_data
            }
        }), 200
        
    except Exception as e:
        logger.error(f"Error al obtener estado de suscripción: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({
            'success': False,
            'error': 'Error al obtener estado de suscripción'
        }), 500

@app.route('/api/subscription/activate-basic', methods=['POST'])
@login_required
def activate_basic_plan():
    """Activar el plan básico Pixelie Basic Plan (gratis)"""
    try:
        # Verificar si el usuario ya tiene una suscripción activa
        existing_subscription = Subscription.query.filter_by(
            user_id=current_user.id,
            status='active'
        ).first()
        
        if existing_subscription:
            # Si ya tiene una suscripción activa, retornar éxito sin crear otra
            return jsonify({
                'success': True,
                'message': 'Ya tienes un plan activo',
                'user': {
                    'subscription': {
                        'plan_type': existing_subscription.plan_type,
                        'status': existing_subscription.status
                    }
                }
            }), 200
        
        # Crear suscripción básica
        basic_subscription = Subscription(
            user_id=current_user.id,
            plan_type='pixelie_basic_plan',
            amount=0.00,
            currency='MXN',
            status='active',
            subscription_id=None,  # No hay ID de Stripe para el plan básico
            current_period_start=datetime.utcnow(),
            current_period_end=None  # El plan básico no expira
        )
        
        db.session.add(basic_subscription)
        db.session.commit()
        
        logger.info(f"Plan básico activado para usuario {current_user.id}")
        
        return jsonify({
            'success': True,
            'message': 'Plan básico activado exitosamente',
            'user': {
                'subscription': {
                    'plan_type': 'pixelie_basic_plan',
                    'status': 'active'
                }
            }
        }), 200
        
    except Exception as e:
        logger.error(f"Error al activar plan básico: {str(e)}")
        logger.error(traceback.format_exc())
        db.session.rollback()
        return jsonify({
            'success': False,
            'error': 'Error al activar plan básico'
        }), 500

@app.route('/api/create-payment-intent', methods=['POST'])
@login_required
def create_payment_intent():
    """Crear Payment Intent de Stripe para el plan Pixelie"""
    try:
        if not stripe.api_key:
            return jsonify({'error': 'Stripe no está configurado'}), 500
        
        plan_price = app.config.get('PIXELIE_PLAN_PRICE', 100.00)
        plan_currency = app.config.get('PIXELIE_PLAN_CURRENCY', 'mxn')
        
        # Convertir a centavos (Stripe usa la unidad más pequeña de la moneda)
        amount_in_cents = int(plan_price * 100) if plan_currency.lower() == 'mxn' else int(plan_price * 100)
        
        # Crear Payment Intent en Stripe
        try:
            intent = stripe.PaymentIntent.create(
                amount=amount_in_cents,
                currency=plan_currency.lower(),
                metadata={
                    'user_id': current_user.id,
                    'user_email': current_user.email,
                    'plan_type': 'pixelie_plan',
                    'plan_name': 'Pixelie Plan'
                },
                description='Pixelie Plan - PixelPick'
            )
            
            # Crear transacción pendiente en la base de datos
            transaction = Transaction(
                user_id=current_user.id,
                transaction_type='subscription',
                amount=plan_price,
                currency=plan_currency.upper(),
                payment_method='stripe',
                payment_intent_id=intent.id,
                status='pending'
            )
            db.session.add(transaction)
            db.session.commit()
            
            logger.info(f"Payment Intent creado para usuario {current_user.id}: {intent.id}")
            
            return jsonify({
                'success': True,
                'user': {
                    'client_secret': intent.client_secret,
                    'payment_intent_id': intent.id
                }
            }), 200
            
        except stripe.error.StripeError as e:
            logger.error(f"Error de Stripe: {str(e)}")
            return jsonify({'error': f'Error al procesar el pago: {str(e)}'}), 500
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error al crear payment intent: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({'error': f'Error al procesar la solicitud: {str(e)}'}), 500

@app.route('/api/stripe-webhook', methods=['POST'])
def stripe_webhook():
    """Webhook de Stripe para confirmar pagos"""
    payload = request.data
    sig_header = request.headers.get('Stripe-Signature')
    webhook_secret = app.config.get('STRIPE_WEBHOOK_SECRET')
    
    if not webhook_secret:
        logger.warning("STRIPE_WEBHOOK_SECRET no configurada, saltando verificación")
        # En desarrollo, podemos procesar sin verificación
        try:
            event = json.loads(payload)
        except:
            return jsonify({'error': 'Invalid payload'}), 400
    else:
        try:
            event = stripe.Webhook.construct_event(
                payload, sig_header, webhook_secret
            )
        except ValueError:
            logger.error("Invalid payload")
            return jsonify({'error': 'Invalid payload'}), 400
        except stripe.error.SignatureVerificationError:
            logger.error("Invalid signature")
            return jsonify({'error': 'Invalid signature'}), 400
    
    # Manejar eventos
    event_type = event.get('type') if isinstance(event, dict) else event['type']
    
    if event_type == 'payment_intent.succeeded':
        payment_intent = event['data']['object']
        payment_intent_id = payment_intent['id']
        
        logger.info(f"Payment Intent exitoso: {payment_intent_id}")
        
        # Buscar transacción en la base de datos
        transaction = Transaction.query.filter_by(
            payment_intent_id=payment_intent_id
        ).first()
        
        if transaction:
            # Actualizar transacción
            transaction.status = 'completed'
            transaction.completed_at = datetime.utcnow()
            
            # Crear o actualizar suscripción
            subscription = Subscription.query.filter_by(
                user_id=transaction.user_id,
                plan_type='pixelie_plan'
            ).first()
            
            if not subscription:
                # Crear nueva suscripción
                subscription = Subscription(
                    user_id=transaction.user_id,
                    plan_type='pixelie_plan',
                    amount=transaction.amount,
                    currency=transaction.currency,
                    status='active',
                    subscription_id=payment_intent_id,
                    current_period_start=datetime.utcnow(),
                    current_period_end=datetime.utcnow() + timedelta(days=365)  # Plan de un año
                )
                db.session.add(subscription)
            else:
                # Actualizar suscripción existente
                subscription.status = 'active'
                subscription.current_period_start = datetime.utcnow()
                subscription.current_period_end = datetime.utcnow() + timedelta(days=365)
            
            db.session.commit()
            logger.info(f"Suscripción activada para usuario {transaction.user_id}")
        else:
            logger.warning(f"Transacción no encontrada para payment_intent_id: {payment_intent_id}")
    
    elif event_type == 'payment_intent.payment_failed':
        payment_intent = event['data']['object']
        payment_intent_id = payment_intent['id']
        
        logger.info(f"Payment Intent fallido: {payment_intent_id}")
        
        # Actualizar transacción como fallida
        transaction = Transaction.query.filter_by(
            payment_intent_id=payment_intent_id
        ).first()
        
        if transaction:
            transaction.status = 'failed'
            db.session.commit()
    
    return jsonify({'status': 'success'}), 200

@app.route('/api/payment-status/<payment_intent_id>', methods=['GET'])
@login_required
def payment_status(payment_intent_id):
    """Verificar estado de un pago"""
    try:
        transaction = Transaction.query.filter_by(
            payment_intent_id=payment_intent_id,
            user_id=current_user.id
        ).first()
        
        if not transaction:
            return jsonify({'error': 'Transacción no encontrada'}), 404
        
        return jsonify({
            'status': transaction.status,
            'transaction': transaction.to_dict()
        }), 200
        
    except Exception as e:
        logger.error(f"Error al verificar estado de pago: {str(e)}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8000)
