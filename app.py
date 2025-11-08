from flask import Flask, render_template, request, jsonify, redirect, url_for, session
from flask_login import LoginManager, login_user, logout_user, login_required, current_user
from flask_mail import Mail, Message
from config import Config
from models import db, User, Game, UserGame, UserPreference
from datetime import datetime, timedelta
import json
import logging
import traceback
import secrets
import threading

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
            except Exception as migration_error:
                logger.warning(f"No se pudieron agregar las nuevas columnas (puede que ya existan): {str(migration_error)}")
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
    return render_template('index.html')

@app.route('/beneficios')
def benefits():
    return render_template('benefits.html')

@app.route('/signin')
def signin():
    if current_user.is_authenticated:
        return redirect(url_for('welcome'))
    return render_template('signin.html')

@app.route('/login')
def login():
    if current_user.is_authenticated:
        return redirect(url_for('welcome'))
    return render_template('login.html')

# ==================== RUTAS PROTEGIDAS ====================

@app.route('/welcome')
@login_required
def welcome():
    return render_template('welcome.html')

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
        user = User(
            first_name=first_name,
            last_name=last_name,
            email=email,
            email_verified=False
        )
        user.set_password(password)
        
        # Generar token de verificación
        verification_token = secrets.token_urlsafe(32)
        user.email_verification_token = verification_token
        user.email_verification_sent_at = datetime.utcnow()
        
        db.session.add(user)
        db.session.commit()
        logger.info(f"Usuario registrado exitosamente: {email}")
        
        # Enviar email de verificación en segundo plano (no bloquea la respuesta)
        try:
            send_verification_email(user, verification_token)
            logger.info(f"Proceso de envío de email iniciado para: {email}")
        except Exception as email_error:
            logger.error(f"Error al iniciar proceso de envío de email: {str(email_error)}")
            logger.error(traceback.format_exc())
            # Continuar aunque falle - el usuario puede solicitar reenvío después
        
        # NO iniciar sesión automáticamente - el usuario debe verificar su email primero
        # login_user(user, remember=True)
        
        return jsonify({
            'success': True,
            'message': 'Usuario registrado exitosamente. Por favor, verifica tu correo electrónico para iniciar sesión.',
            'email_sent': True,
            'requires_verification': True
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
        
        # Verificar si el email está verificado
        email_verified = getattr(user, 'email_verified', False)
        if not email_verified:
            return jsonify({
                'error': 'Por favor, verifica tu correo electrónico antes de iniciar sesión. Revisa tu bandeja de entrada.',
                'requires_verification': True,
                'email': user.email
            }), 403
        
        # Iniciar sesión
        login_user(user, remember=True)
        
        return jsonify({
            'success': True,
            'message': 'Inicio de sesión exitoso',
            'user': user.to_dict()
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
        
        # Obtener juegos del usuario
        user_games = UserGame.query.filter_by(user_id=user.id).order_by(UserGame.last_played.desc()).limit(10).all()
        games_data = [ug.to_dict() for ug in user_games]
        
        # Estadísticas
        completed_count = UserGame.query.filter_by(user_id=user.id, status='completed').count()
        playing_count = UserGame.query.filter_by(user_id=user.id, status='playing').count()
        
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
    """Obtener recomendaciones de juegos para el usuario"""
    try:
        # Por ahora, devolvemos los primeros 3 juegos
        # En el futuro, aquí se implementaría la lógica de IA
        games = Game.query.limit(3).all()
        games_data = [game.to_dict() for game in games]
        
        return jsonify({
            'success': True,
            'recommendations': games_data
        }), 200
        
    except Exception as e:
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
    """Agregar juego al usuario"""
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
        if existing:
            # Actualizar estado y fecha
            existing.status = status
            existing.last_played = datetime.utcnow()
        else:
            # Crear nueva relación
            user_game = UserGame(
                user_id=current_user.id,
                game_id=game_id,
                status=status
            )
            db.session.add(user_game)
        
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': 'Juego agregado exitosamente'
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': f'Error al agregar juego: {str(e)}'}), 500

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

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8000)
