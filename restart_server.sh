#!/bin/bash

# Script para reiniciar el servidor Flask

echo "🛑 Deteniendo servidor Flask si está corriendo..."
lsof -ti:8000 | xargs kill -9 2>/dev/null
sleep 2

echo "🚀 Iniciando servidor Flask..."
cd "$(dirname "$0")"
python3 app.py

