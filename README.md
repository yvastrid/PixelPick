# PixelPick - Landing Page

Landing page moderna y animada para PixelPick, una plataforma de suscripción de juegos con recomendaciones impulsadas por IA.

## 🚀 Características

- Diseño moderno y oscuro inspirado en Luma, adaptado para PixelPick
- Animaciones y efectos visuales atractivos:
  - Iconos flotantes con movimiento suave
  - Efectos de parallax con el mouse
  - Gradientes animados
  - Efectos de brillo y resplandor
  - Animaciones de entrada suaves
- Diseño responsive
- Mockup de teléfono con interfaz de juego

## 📋 Requisitos

- Python 3.8 o superior
- pip (gestor de paquetes de Python)

## 🛠️ Instalación Local

1. Clona o descarga este repositorio

2. Instala las dependencias:
```bash
pip install -r requirements.txt
```

3. Ejecuta la aplicación:
```bash
python app.py
```

4. Abre tu navegador en: `http://localhost:5000`

## ☁️ Despliegue en la Nube

### Opción 1: Render (Recomendado - Gratis y Fácil) ⭐

**Render ofrece hosting gratuito con despliegue automático desde GitHub.**

#### Pasos:

1. **Crea una cuenta en [Render.com](https://render.com)** (puedes usar GitHub para registrarte)

2. **Prepara tu código en GitHub:**
   ```bash
   # Si aún no tienes un repositorio en GitHub:
   git init
   git add .
   git commit -m "Initial commit"
   # Crea un nuevo repositorio en GitHub y luego:
   git remote add origin https://github.com/TU_USUARIO/PixelPick.git
   git branch -M main
   git push -u origin main
   ```

3. **En Render:**
   - Haz clic en "New +" → "Web Service"
   - Conecta tu repositorio de GitHub
   - Configuración:
     - **Name**: `pixelpick` (o el nombre que prefieras)
     - **Environment**: `Python 3`
     - **Build Command**: `pip install -r requirements.txt`
     - **Start Command**: `gunicorn app:app`
     - **Plan**: Free (gratis)

4. **Haz clic en "Create Web Service"**

5. **Espera a que termine el despliegue** (5-10 minutos la primera vez)

6. **¡Listo!** Tu sitio estará disponible en `https://pixelpick.onrender.com` (o el nombre que hayas elegido)

**Nota:** En el plan gratuito, la aplicación puede "dormirse" después de 15 minutos de inactividad. La primera carga después de dormir puede tardar ~30 segundos.

---

### Opción 2: Railway (Gratis y Rápido) 🚂

1. **Crea una cuenta en [Railway.app](https://railway.app)** (con GitHub)

2. **Nuevo Proyecto:**
   - Haz clic en "New Project"
   - Selecciona "Deploy from GitHub repo"
   - Elige tu repositorio

3. **Railway detectará automáticamente Flask** y configurará todo

4. **Si necesitas configurar manualmente:**
   - **Start Command**: `gunicorn app:app --bind 0.0.0.0:$PORT`

5. **Tu sitio estará disponible** en una URL como `https://pixelpick-production.up.railway.app`

---

### Opción 3: PythonAnywhere (Gratis para principiantes) 🐍

1. **Crea una cuenta en [PythonAnywhere.com](https://www.pythonanywhere.com)**

2. **Sube tus archivos:**
   - Ve a "Files"
   - Sube todos los archivos de tu proyecto

3. **Configura el Web App:**
   - Ve a "Web" → "Add a new web app"
   - Selecciona Flask y Python 3.10
   - Ruta del código: `/home/TU_USUARIO/mysite/`
   - Ruta del WSGI: `/var/www/TU_USUARIO_pythonanywhere_com_wsgi.py`

4. **Edita el archivo WSGI:**
   ```python
   import sys
   path = '/home/TU_USUARIO/mysite'
   if path not in sys.path:
       sys.path.append(path)
   
   from app import app as application
   ```

5. **Recarga la aplicación** y estará disponible en `TU_USUARIO.pythonanywhere.com`

---

### Opción 4: Fly.io (Gratis con tarjeta de crédito) ✈️

1. **Instala Fly CLI:**
   ```bash
   curl -L https://fly.io/install.sh | sh
   ```

2. **Crea un archivo `fly.toml`** en la raíz:
   ```toml
   app = "pixelpick"
   primary_region = "iad"
   
   [build]
   
   [http_service]
     internal_port = 8000
     force_https = true
     auto_stop_machines = true
     auto_start_machines = true
     min_machines_running = 0
   
   [[vm]]
     memory_mb = 256
   ```

3. **Despliega:**
   ```bash
   fly auth signup
   fly launch
   fly deploy
   ```

---

### Opción 5: Vercel (Para proyectos pequeños) ▲

1. **Instala Vercel CLI:**
   ```bash
   npm i -g vercel
   ```

2. **Crea `vercel.json`:**
   ```json
   {
     "version": 2,
     "builds": [
       {
         "src": "app.py",
         "use": "@vercel/python"
       }
     ],
     "routes": [
       {
         "src": "/(.*)",
         "dest": "app.py"
       }
     ]
   }
   ```

3. **Despliega:**
   ```bash
   vercel
   ```

---

## 📝 Checklist antes de desplegar

- [ ] Verifica que `requirements.txt` incluya todas las dependencias
- [ ] Asegúrate de que `Procfile` existe (ya lo tienes)
- [ ] Verifica que el logo esté en `static/images/logo.png`
- [ ] Prueba la aplicación localmente antes de desplegar
- [ ] Si usas variables de entorno, configúralas en la plataforma

## 🔗 URLs después del despliegue

Una vez desplegado, tu aplicación estará disponible en:
- **Render**: `https://TU_APP.onrender.com`
- **Railway**: `https://TU_APP.up.railway.app`
- **PythonAnywhere**: `https://TU_USUARIO.pythonanywhere.com`
- **Fly.io**: `https://TU_APP.fly.dev`
- **Vercel**: `https://TU_APP.vercel.app`

## 📁 Estructura del Proyecto

```
PixelPick/
├── app.py                 # Aplicación Flask principal
├── requirements.txt       # Dependencias de Python
├── README.md             # Este archivo
├── Images/               # Logo original
│   └── logo.png
├── static/               # Archivos estáticos
│   ├── css/
│   │   └── style.css     # Estilos y animaciones
│   ├── js/
│   │   └── main.js       # JavaScript para interacciones
│   └── images/
│       └── logo.png      # Logo de PixelPick
└── templates/            # Plantillas HTML
    └── index.html        # Página principal
```

## 🎨 Personalización

- **Colores**: Edita las variables CSS en `static/css/style.css` (líneas 7-15)
- **Texto**: Modifica el contenido en `templates/index.html`
- **Animaciones**: Ajusta los keyframes en `static/css/style.css`
- **Efectos**: Personaliza las interacciones en `static/js/main.js`

## 📝 Notas

- Asegúrate de que el logo esté en `static/images/logo.png`
- El sitio está optimizado para navegadores modernos
- Las animaciones están optimizadas para rendimiento

## 🔧 Solución de Problemas

Si el logo no aparece:
- Verifica que `static/images/logo.png` existe
- Revisa la ruta en `templates/index.html` (línea 15)

Si las animaciones no funcionan:
- Verifica que JavaScript esté habilitado en tu navegador
- Revisa la consola del navegador para errores

## 📄 Licencia

Este proyecto es de uso personal/propietario.

