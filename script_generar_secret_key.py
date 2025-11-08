#!/usr/bin/env python3
"""
Script para generar una SECRET_KEY segura para PixelPick
Ejecuta este script para generar una clave secreta aleatoria y segura.
"""

import secrets

def generar_secret_key():
    """Genera una clave secreta segura de 32 bytes"""
    return secrets.token_urlsafe(32)

if __name__ == "__main__":
    secret_key = generar_secret_key()
    print("\n" + "="*60)
    print("SECRET_KEY generada:")
    print("="*60)
    print(secret_key)
    print("="*60)
    print("\n⚠️  IMPORTANTE:")
    print("1. Copia esta clave y guárdala en un lugar seguro")
    print("2. Úsala como valor de la variable de entorno SECRET_KEY")
    print("3. NO la compartas públicamente")
    print("4. Usa una clave diferente para desarrollo y producción")
    print("\n")

