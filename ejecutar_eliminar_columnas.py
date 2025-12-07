#!/usr/bin/env python3
"""
Script para eliminar las columnas email_verification_token y email_verification_sent_at
de la base de datos PostgreSQL en Render
"""

import os
import sys
from dotenv import load_dotenv

# Cargar variables de entorno
load_dotenv()

def ejecutar_sql():
    """Ejecuta el SQL para eliminar las columnas"""
    try:
        # Obtener DATABASE_URL de las variables de entorno
        database_url = os.environ.get('DATABASE_URL')
        
        if not database_url:
            print("❌ Error: DATABASE_URL no está configurada")
            print("   Asegúrate de tener la variable DATABASE_URL en tu entorno")
            return False
        
        print(f"📊 Conectando a la base de datos...")
        print(f"   URL: {database_url.split('@')[0]}@***")  # Ocultar contraseña
        
        # Importar psycopg para PostgreSQL
        try:
            import psycopg
        except ImportError:
            print("❌ Error: psycopg no está instalado")
            print("   Instala con: pip install psycopg[binary]")
            return False
        
        # Parsear la URL de conexión
        # Formato: postgresql://user:password@host:port/database
        if database_url.startswith('postgres://'):
            database_url = database_url.replace('postgres://', 'postgresql://', 1)
        
        # Conectar a la base de datos
        with psycopg.connect(database_url) as conn:
            with conn.cursor() as cur:
                print("\n🔍 Verificando columnas existentes...")
                
                # Verificar si las columnas existen
                cur.execute("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'users' 
                    AND column_name IN ('email_verification_token', 'email_verification_sent_at')
                    ORDER BY column_name;
                """)
                
                columnas_existentes = [row[0] for row in cur.fetchall()]
                
                if not columnas_existentes:
                    print("✅ Las columnas ya no existen en la base de datos")
                    return True
                
                print(f"   Columnas encontradas: {', '.join(columnas_existentes)}")
                
                # Eliminar las columnas
                print("\n🗑️  Eliminando columnas...")
                
                if 'email_verification_token' in columnas_existentes:
                    print("   - Eliminando email_verification_token...")
                    cur.execute("ALTER TABLE users DROP COLUMN IF EXISTS email_verification_token;")
                    print("   ✅ email_verification_token eliminada")
                
                if 'email_verification_sent_at' in columnas_existentes:
                    print("   - Eliminando email_verification_sent_at...")
                    cur.execute("ALTER TABLE users DROP COLUMN IF EXISTS email_verification_sent_at;")
                    print("   ✅ email_verification_sent_at eliminada")
                
                # Confirmar cambios
                conn.commit()
                
                print("\n✅ Columnas eliminadas exitosamente")
                
                # Verificar que fueron eliminadas
                print("\n🔍 Verificando eliminación...")
                cur.execute("""
                    SELECT column_name 
                    FROM information_schema.columns 
                    WHERE table_name = 'users' 
                    AND column_name IN ('email_verification_token', 'email_verification_sent_at');
                """)
                
                columnas_restantes = cur.fetchall()
                
                if not columnas_restantes:
                    print("✅ Confirmado: Las columnas fueron eliminadas correctamente")
                else:
                    print(f"⚠️  Advertencia: Aún existen columnas: {[r[0] for r in columnas_restantes]}")
                
                # Mostrar todas las columnas de la tabla users
                print("\n📋 Columnas actuales en la tabla 'users':")
                cur.execute("""
                    SELECT column_name, data_type 
                    FROM information_schema.columns 
                    WHERE table_name = 'users' 
                    ORDER BY ordinal_position;
                """)
                
                columnas = cur.fetchall()
                for col_name, col_type in columnas:
                    print(f"   - {col_name} ({col_type})")
                
                return True
                
    except Exception as e:
        print(f"\n❌ Error al ejecutar el script: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
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
                sys.exit(0)
        except EOFError:
            # Si no hay entrada disponible (ejecución automática), continuar
            print("\n✅ Ejecutando automáticamente (sin confirmación)...")
    
    print("\n" + "=" * 60)
    
    exito = ejecutar_sql()
    
    print("\n" + "=" * 60)
    if exito:
        print("✅ Script ejecutado exitosamente")
        sys.exit(0)
    else:
        print("❌ El script falló")
        sys.exit(1)

