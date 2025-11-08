from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash

db = SQLAlchemy()

class User(UserMixin, db.Model):
    """Modelo de Usuario"""
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    first_name = db.Column(db.String(100), nullable=False)
    last_name = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False, index=True)
    password_hash = db.Column(db.String(255), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Campos para controlar cambios de nombre/apellido
    name_change_count = db.Column(db.Integer, default=0)  # Número de veces que ha cambiado el nombre
    last_name_change_date = db.Column(db.DateTime, nullable=True)  # Fecha del último cambio
    
    # Relaciones
    user_games = db.relationship('UserGame', backref='user', lazy=True, cascade='all, delete-orphan')
    preferences = db.relationship('UserPreference', backref='user', lazy=True, cascade='all, delete-orphan')
    
    def set_password(self, password):
        """Genera hash de la contraseña"""
        self.password_hash = generate_password_hash(password)
    
    def check_password(self, password):
        """Verifica la contraseña"""
        return check_password_hash(self.password_hash, password)
    
    def to_dict(self):
        """Convierte el usuario a diccionario"""
        # Usar getattr para manejar columnas que pueden no existir aún
        name_change_count = getattr(self, 'name_change_count', None) or 0
        last_name_change_date = getattr(self, 'last_name_change_date', None)
        
        return {
            'id': self.id,
            'first_name': self.first_name,
            'last_name': self.last_name,
            'email': self.email,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'name_change_count': name_change_count,
            'last_name_change_date': last_name_change_date.isoformat() if last_name_change_date else None
        }
    
    def can_change_name(self):
        """Verifica si el usuario puede cambiar su nombre/apellido"""
        # Usar getattr para manejar columnas que pueden no existir aún
        name_change_count = getattr(self, 'name_change_count', None) or 0
        last_name_change_date = getattr(self, 'last_name_change_date', None)
        
        # Si no ha hecho cambios, puede cambiar
        if name_change_count == 0:
            return True, None
        
        # Si ya hizo 3 cambios, verificar si han pasado 60 días
        if name_change_count >= 3:
            if last_name_change_date:
                days_since_last_change = (datetime.utcnow() - last_name_change_date).days
                if days_since_last_change < 60:
                    days_remaining = 60 - days_since_last_change
                    return False, f"Has excedido el límite de 3 cambios. Debes esperar {days_remaining} días más."
                else:
                    # Han pasado 60 días, resetear contador
                    if hasattr(self, 'name_change_count'):
                        self.name_change_count = 0
                        self.last_name_change_date = None
                        db.session.commit()
                    return True, None
        
        # Si tiene menos de 3 cambios, puede cambiar
        return True, None

class Game(db.Model):
    """Modelo de Juego"""
    __tablename__ = 'games'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text)
    price = db.Column(db.Numeric(10, 2), default=0.00)
    platforms = db.Column(db.String(200))  # JSON string o comma-separated
    image_url = db.Column(db.String(500))
    category = db.Column(db.String(100))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relaciones
    user_games = db.relationship('UserGame', backref='game', lazy=True, cascade='all, delete-orphan')
    
    def to_dict(self):
        """Convierte el juego a diccionario"""
        return {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'price': float(self.price) if self.price else 0.0,
            'platforms': self.platforms.split(',') if self.platforms else [],
            'image_url': self.image_url,
            'category': self.category
        }

class UserGame(db.Model):
    """Relación entre Usuario y Juego (juegos del usuario)"""
    __tablename__ = 'user_games'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    game_id = db.Column(db.Integer, db.ForeignKey('games.id'), nullable=False)
    status = db.Column(db.String(50), default='playing')  # playing, completed, wishlist
    last_played = db.Column(db.DateTime, default=datetime.utcnow)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Índice único para evitar duplicados
    __table_args__ = (db.UniqueConstraint('user_id', 'game_id', name='unique_user_game'),)
    
    def to_dict(self):
        """Convierte la relación a diccionario"""
        return {
            'id': self.id,
            'user_id': self.user_id,
            'game_id': self.game_id,
            'status': self.status,
            'last_played': self.last_played.isoformat() if self.last_played else None,
            'game': self.game.to_dict() if self.game else None
        }

class UserPreference(db.Model):
    """Preferencias del usuario para recomendaciones IA"""
    __tablename__ = 'user_preferences'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    preference_type = db.Column(db.String(50))  # genre, platform, price_range, etc.
    preference_value = db.Column(db.String(200))
    weight = db.Column(db.Float, default=1.0)  # Peso de la preferencia
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    def to_dict(self):
        """Convierte la preferencia a diccionario"""
        return {
            'id': self.id,
            'preference_type': self.preference_type,
            'preference_value': self.preference_value,
            'weight': self.weight
        }

