import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    """Configuración de la aplicación"""
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'dev-secret-key-change-in-production'
    
    # Configuración de base de datos
    # Para desarrollo local, usa SQLite
    # Para producción, usa PostgreSQL (variable DATABASE_URL)
    DATABASE_URL = os.environ.get('DATABASE_URL')
    
    if DATABASE_URL:
        # PostgreSQL (producción)
        # Heroku y otros proveedores proporcionan DATABASE_URL en formato:
        # postgresql://user:password@host:port/database
        # Usamos psycopg (psycopg3) que es compatible con Python 3.13
        db_url = DATABASE_URL.replace('postgres://', 'postgresql+psycopg://', 1)
        # Si ya tiene postgresql://, solo agregamos +psycopg
        if db_url.startswith('postgresql://') and '+psycopg' not in db_url:
            db_url = db_url.replace('postgresql://', 'postgresql+psycopg://', 1)
        SQLALCHEMY_DATABASE_URI = db_url
    else:
        # SQLite (desarrollo local)
        SQLALCHEMY_DATABASE_URI = os.environ.get('SQLALCHEMY_DATABASE_URI') or 'sqlite:///pixelpick.db'
    
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SQLALCHEMY_ENGINE_OPTIONS = {
        'pool_pre_ping': True,
        'pool_recycle': 300,
    }

