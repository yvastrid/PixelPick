#!/usr/bin/env python3
"""
Script para eliminar las columnas email_verification_token y email_verification_sent_at
de la base de datos PostgreSQL en Render usando la configuración de Flask
"""

import sys
from config import Config
from sqlalchemy import text, inspect

def ejecutar_sql():
    """Ejecuta el SQL para eliminar las columnas usando SQLAlchemy"""
    try:
        from app import app, db
        
        print("=" * 60)
        print("🗑️  Script de Limpieza de Base de Datos")
        print("=" * 60)
        print("\nEste script eliminará las siguientes columnas de la tabla 'users':")
        print("  - email_verification_token")
        print("  - email_verification_sent_at")
        print("\n⚠️  Esta acción no se puede deshacer.")
        
        # Permitir ejecución automática con --yes o -y
        if len(sys.argv) > 1 and sys.argv[1] in ['--yes', '-y', '--force']:
            print("\n✅ Ejecutando automáticamente...")
        else:
            try:
                respuesta = input("\n¿Deseas continuar? (s/n): ").strip().lower()
                if respuesta not in ['s', 'si', 'sí', 'y', 'yes']:
                    print("❌ Operación cancelada")
                    return False
            except EOFError:
                # Si no hay entrada disponible (ejecución automática), continuar
                print("\n✅ Ejecutando automáticamente (sin confirmación)...")
        
        print("\n" + "=" * 60)
        print(f"📊 Conectando a la base de datos...")
        
        # Obtener URL de la base de datos (ocultar credenciales)
        db_url = Config.SQLALCHEMY_DATABASE_URI
        if '@' in db_url:
            db_url_display = db_url.split('@')[0].split('://')[0] + '://***@' + '@'.join(db_url.split('@')[1:])
        else:
            db_url_display = db_url[:50] + '...' if len(db_url) > 50 else db_url
        print(f"   URL: {db_url_display}")
        
        with app.app_context():
            # Verificar conexión
            inspector = inspect(db.engine)
            
            print("\n🔍 Verificando columnas existentes...")
            
            # Obtener todas las columnas de la tabla users
            columns = [col['name'] for col in inspector.get_columns('users')]
            
            columnas_a_eliminar = []
            if 'email_verification_token' in columns:
                columnas_a_eliminar.append('email_verification_token')
            if 'email_verification_sent_at' in columns:
                columnas_a_eliminar.append('email_verification_sent_at')
            
            if not columnas_a_eliminar:
                print("✅ Las columnas ya no existen en la base de datos")
                return True
            
            print(f"   Columnas encontradas: {', '.join(columnas_a_eliminar)}")
            
            # Eliminar las columnas
            print("\n🗑️  Eliminando columnas...")
            
            for columna in columnas_a_eliminar:
                print(f"   - Eliminando {columna}...")
                try:
                    db.session.execute(text(f"ALTER TABLE users DROP COLUMN IF EXISTS {columna};"))
                    print(f"   ✅ {columna} eliminada")
                except Exception as e:
                    print(f"   ⚠️  Error al eliminar {columna}: {str(e)}")
                    # Continuar con las demás columnas
            
            # Confirmar cambios
            db.session.commit()
            
            print("\n✅ Columnas eliminadas exitosamente")
            
            # Verificar que fueron eliminadas
            print("\n🔍 Verificando eliminación...")
            columns_after = [col['name'] for col in inspector.get_columns('users')]
            
            columnas_restantes = []
            if 'email_verification_token' in columns_after:
                columnas_restantes.append('email_verification_token')
            if 'email_verification_sent_at' in columns_after:
                columnas_restantes.append('email_verification_sent_at')
            
            if not columnas_restantes:
                print("✅ Confirmado: Las columnas fueron eliminadas correctamente")
            else:
                print(f"⚠️  Advertencia: Aún existen columnas: {', '.join(columnas_restantes)}")
            
            # Mostrar todas las columnas de la tabla users
            print("\n📋 Columnas actuales en la tabla 'users':")
            for col in columns_after:
                col_info = next((c for c in inspector.get_columns('users') if c['name'] == col), None)
                col_type = col_info['type'] if col_info else 'unknown'
                print(f"   - {col} ({col_type})")
            
            return True
                
    except Exception as e:
        print(f"\n❌ Error al ejecutar el script: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    exito = ejecutar_sql()
    
    print("\n" + "=" * 60)
    if exito:
        print("✅ Script ejecutado exitosamente")
        sys.exit(0)
    else:
        print("❌ El script falló")
        sys.exit(1)

