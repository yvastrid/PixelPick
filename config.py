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
    
    # Configuración de email (Flask-Mail)
    MAIL_SERVER = os.environ.get('MAIL_SERVER') or 'smtp.gmail.com'
    MAIL_PORT = int(os.environ.get('MAIL_PORT') or 587)
    MAIL_USE_TLS = os.environ.get('MAIL_USE_TLS', 'true').lower() in ['true', 'on', '1']
    MAIL_USE_SSL = os.environ.get('MAIL_USE_SSL', 'false').lower() in ['true', 'on', '1']
    MAIL_USERNAME = os.environ.get('MAIL_USERNAME')
    MAIL_PASSWORD = os.environ.get('MAIL_PASSWORD')
    MAIL_DEFAULT_SENDER = os.environ.get('MAIL_DEFAULT_SENDER') or MAIL_USERNAME
    
    # URL base de la aplicación (para links en emails)
    APP_URL = os.environ.get('APP_URL') or 'https://pixelpick-akp2.onrender.com'
    
    # Configuración de Stripe
    STRIPE_PUBLIC_KEY = os.environ.get('STRIPE_PUBLIC_KEY')
    STRIPE_SECRET_KEY = os.environ.get('STRIPE_SECRET_KEY')
    STRIPE_WEBHOOK_SECRET = os.environ.get('STRIPE_WEBHOOK_SECRET')
    
    # Plan de suscripción Pixelie Plan
    PIXELIE_PLAN_PRICE = 100.00  # MXN
    PIXELIE_PLAN_CURRENCY = 'mxn'

