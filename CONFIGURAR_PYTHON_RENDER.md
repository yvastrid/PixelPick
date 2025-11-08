# 🔧 Configurar Python 3.12 en Render

## 🔍 Problema

Render está usando Python 3.13.4 por defecto, pero `psycopg2-binary` no es compatible con Python 3.13.

## ✅ Solución: Configurar Python en Render Dashboard

Ya que `runtime.txt` no está siendo detectado automáticamente, puedes configurar la versión de Python directamente en Render:

### Opción 1: Configurar en el Dashboard (Más Fácil) ⭐

1. Ve a tu servicio web en Render: https://dashboard.render.com
2. Haz clic en tu servicio web (pixelpick-akp2)
3. Ve a la sección **"Settings"** (Configuración)
4. Busca la opción **"Python Version"** o **"Environment"**
5. Cambia de **"Auto"** o **"3.13"** a **"3.12"** o **"Python 3.12"**
6. Guarda los cambios
7. Render re-desplegará automáticamente

### Opción 2: Usar runtime.txt (Ya está creado)

El archivo `runtime.txt` ya está en tu repositorio con el contenido:
```
python-3.12
```

Si Render no lo detecta automáticamente, puedes:

1. **Verificar que esté en la raíz del proyecto** (ya está)
2. **Forzar un nuevo despliegue** después de asegurarte de que el archivo esté en GitHub
3. **Verificar en los logs** que Render lo detecte

### Opción 3: Especificar en el Build Command

Puedes modificar el Build Command en Render para especificar Python:

1. Ve a Settings → Build Command
2. Cambia a:
   ```
   python3.12 -m pip install -r requirements.txt
   ```

**Nota**: Esta opción requiere que Python 3.12 esté disponible en el sistema.

## 🚀 Pasos Recomendados

**Usa la Opción 1** (configurar en el Dashboard) ya que es la más confiable:

1. Ve a Render Dashboard
2. Selecciona tu servicio web
3. Settings → Python Version → Selecciona "3.12"
4. Guarda
5. Espera a que re-despliegue

## ✅ Verificación

Después de configurar, en los logs deberías ver:
```
==> Installing Python version 3.12.x...
```

En lugar de:
```
==> Installing Python version 3.13.4...
```

## 📝 Nota

Si después de configurar Python 3.12 en Render, el despliegue sigue fallando, puede ser por otro motivo. Comparte los logs completos del error para diagnosticar.

