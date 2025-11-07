# 🚀 Guía Paso a Paso: Desplegar PixelPick en Render

## 📋 Paso 1: Preparar tu código para GitHub

### 1.1 Inicializar Git (si no lo has hecho)

Abre tu terminal en la carpeta del proyecto y ejecuta:

```bash
cd /Users/astridmarquez/Downloads/PixelPick
git init
```

### 1.2 Agregar todos los archivos

```bash
git add .
```

### 1.3 Hacer tu primer commit

```bash
git commit -m "Initial commit - PixelPick landing page"
```

---

## 📦 Paso 2: Crear repositorio en GitHub

### 2.1 Crear cuenta en GitHub (si no tienes)

1. Ve a [github.com](https://github.com) y crea una cuenta gratuita

### 2.2 Crear nuevo repositorio

1. Haz clic en el botón **"+"** (arriba a la derecha) → **"New repository"**
2. **Repository name**: `PixelPick` (o el nombre que prefieras)
3. **Description**: "Plataforma de juegos con recomendaciones IA"
4. **Visibility**: Elige **Public** (gratis) o **Private** (si tienes cuenta Pro)
5. **NO marques** "Add a README file" (ya tienes uno)
6. **NO marques** "Add .gitignore" (ya tienes uno)
7. Haz clic en **"Create repository"**

### 2.3 Conectar tu proyecto local con GitHub

GitHub te mostrará comandos. Ejecuta estos en tu terminal:

```bash
# Reemplaza TU_USUARIO con tu nombre de usuario de GitHub
git remote add origin https://github.com/TU_USUARIO/PixelPick.git
git branch -M main
git push -u origin main
```

**Nota:** Te pedirá tu usuario y contraseña de GitHub. Si tienes autenticación de dos factores, necesitarás un token de acceso personal.

---

## 🔐 Paso 2.5: Crear Token de Acceso (si es necesario)

Si GitHub te pide autenticación:

1. Ve a GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Haz clic en **"Generate new token (classic)"**
3. **Note**: "PixelPick deployment"
4. Selecciona el scope **"repo"** (marca la casilla)
5. Haz clic en **"Generate token"**
6. **Copia el token** (solo se muestra una vez)
7. Cuando hagas `git push`, usa tu **usuario de GitHub** y el **token** como contraseña

---

## ☁️ Paso 3: Configurar Render

### 3.1 Crear cuenta en Render

1. Ve a [render.com](https://render.com)
2. Haz clic en **"Get Started for Free"**
3. Elige **"Sign up with GitHub"** (es la opción más fácil)
4. Autoriza a Render a acceder a tus repositorios

### 3.2 Crear Web Service

1. En el dashboard de Render, haz clic en **"New +"** (arriba a la derecha)
2. Selecciona **"Web Service"**

### 3.3 Conectar tu repositorio

1. En "Connect a repository", busca y selecciona **"PixelPick"** (tu repositorio)
2. Haz clic en **"Connect"**

### 3.4 Configurar el servicio

Completa estos campos:

- **Name**: `pixelpick` (o el nombre que prefieras)
- **Region**: Elige la más cercana (ej: `Oregon (US West)` para México)
- **Branch**: `main` (debe estar seleccionado automáticamente)
- **Root Directory**: Déjalo vacío (o `.` si te lo pide)
- **Environment**: `Python 3`
- **Build Command**: 
  ```
  pip install -r requirements.txt
  ```
- **Start Command**: 
  ```
  gunicorn app:app
  ```
- **Plan**: Selecciona **"Free"** (gratis)

### 3.5 Variables de entorno (opcional)

Por ahora no necesitas configurar ninguna variable de entorno. Déjalo vacío.

### 3.6 Crear el servicio

1. Haz clic en **"Create Web Service"** (abajo)
2. Render comenzará a construir y desplegar tu aplicación
3. Esto tomará **5-10 minutos** la primera vez

---

## ✅ Paso 4: Verificar el despliegue

### 4.1 Revisar el log

En Render, verás un log en tiempo real. Busca:
- ✅ "Build successful"
- ✅ "Your service is live"

### 4.2 Acceder a tu sitio

Una vez completado, tu sitio estará disponible en:
```
https://pixelpick.onrender.com
```
(O el nombre que hayas elegido)

### 4.3 Probar tu sitio

1. Abre la URL en tu navegador
2. Verifica que todas las páginas funcionen:
   - Página principal (`/`)
   - Página de beneficios (`/beneficios`)
   - Página de login (`/login`)
   - Página de inicio de sesión (`/signin`)
   - Página de bienvenida (`/welcome`)
   - Página de perfil (`/profile`)
   - Página de configuración (`/settings`)

---

## 🔄 Paso 5: Actualizaciones futuras

Cada vez que hagas cambios:

1. **En tu computadora:**
   ```bash
   git add .
   git commit -m "Descripción de los cambios"
   git push
   ```

2. **Render detectará automáticamente** los cambios y volverá a desplegar

---

## ⚠️ Notas importantes

### Plan Gratuito de Render:
- ✅ Tu app puede "dormirse" después de 15 minutos de inactividad
- ⏱️ La primera carga después de dormir puede tardar ~30 segundos
- 📊 Tienes 750 horas gratis al mes (suficiente para un demo)

### Si algo falla:
1. Revisa los **logs** en Render (pestaña "Logs")
2. Verifica que todos los archivos estén en GitHub
3. Asegúrate de que `requirements.txt` tenga todas las dependencias
4. Verifica que `Procfile` exista y tenga el contenido correcto

---

## 🆘 Solución de problemas comunes

### Error: "Module not found"
- Verifica que todas las dependencias estén en `requirements.txt`

### Error: "Port already in use"
- Render maneja esto automáticamente, no deberías tener este problema

### Error: "Build failed"
- Revisa los logs en Render
- Verifica que Python 3 esté seleccionado
- Asegúrate de que `gunicorn` esté en `requirements.txt` (ya está)

### El sitio carga pero no se ven los estilos
- Verifica que la carpeta `static/` esté en GitHub
- Revisa las rutas en los templates HTML

---

## 📞 ¿Necesitas ayuda?

Si tienes algún problema durante el despliegue:
1. Revisa los logs en Render
2. Verifica que todos los archivos estén en GitHub
3. Asegúrate de seguir cada paso exactamente

¡Tu sitio estará en línea en menos de 15 minutos! 🎉

