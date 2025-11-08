from flask import Flask, render_template, request, jsonify, redirect, url_for, session
from flask_login import LoginManager, login_user, logout_user, login_required, current_user
from config import Config
from models import db, User, Game, UserGame, UserPreference
from datetime import datetime
import json
import logging
import traceback

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
app.config.from_object(Config)

# Inicializar extensiones
db.init_app(app)
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
            email=email
        )
        user.set_password(password)
        
        db.session.add(user)
        db.session.commit()
        logger.info(f"Usuario registrado exitosamente: {email}")
        
        # Iniciar sesión automáticamente
        login_user(user, remember=True)
        
        return jsonify({
            'success': True,
            'message': 'Usuario registrado exitosamente',
            'user': user.to_dict()
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
    """Actualizar perfil del usuario"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No se recibieron datos'}), 400
        
        user = current_user
        
        # Actualizar campos permitidos
        if 'firstName' in data:
            user.first_name = data['firstName'].strip()
        if 'lastName' in data:
            user.last_name = data['lastName'].strip()
        
        user.updated_at = datetime.utcnow()
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': 'Perfil actualizado exitosamente',
            'user': user.to_dict()
        }), 200
        
    except Exception as e:
        db.session.rollback()
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
    
    return jsonify({
        'status': 'ok',
        'database': db_status,
        'database_url': db_url_display,
        'secret_key_configured': bool(app.config.get('SECRET_KEY') and app.config.get('SECRET_KEY') != 'dev-secret-key-change-in-production')
    }), 200

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8000)
