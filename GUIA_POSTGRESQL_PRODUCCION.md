# 🗄️ Guía Completa: Configurar PostgreSQL en Producción

Esta guía te ayudará paso a paso a configurar PostgreSQL y desplegar PixelPick en producción.

---

## 📋 Índice

1. [Render (Recomendado - Más Fácil)](#1-render-recomendado)
2. [Railway](#2-railway)
3. [Heroku](#3-heroku)
4. [Generar SECRET_KEY](#generar-secret_key)

---

## 1. Render (Recomendado) ⭐

### Paso 1: Crear Base de Datos PostgreSQL

1. **Ve a tu dashboard de Render**: [dashboard.render.com](https://dashboard.render.com)

2. **Crear nueva base de datos**:
   - Haz clic en **"New +"** (arriba a la derecha)
   - Selecciona **"PostgreSQL"**

3. **Configurar la base de datos**:
   - **Name**: `pixelpick-db` (o el nombre que prefieras)
   - **Database**: `pixelpick` (o déjalo por defecto)
   - **User**: Se genera automáticamente
   - **Region**: Elige la misma región que tu aplicación web
   - **PostgreSQL Version**: `16` (o la más reciente)
   - **Plan**: 
     - **Free** (gratis, para pruebas)
     - **Starter** ($7/mes, recomendado para producción)

4. **Crear la base de datos**:
   - Haz clic en **"Create Database"**
   - ⏱️ Espera 2-3 minutos mientras se crea

5. **Copiar la URL de conexión**:
   - Una vez creada, verás una sección **"Connections"**
   - Copia la **"Internal Database URL"** o **"External Database URL"**
   - Se ve así: `postgresql://usuario:contraseña@host:puerto/nombre_db`
   - **⚠️ IMPORTANTE**: Guarda esta URL, la necesitarás en el siguiente paso

### Paso 2: Configurar Variables de Entorno en tu Web Service

1. **Ve a tu Web Service en Render**:
   - Si aún no lo has creado, sigue la guía en `GUIA_DESPLIEGUE.md`
   - Si ya lo tienes, haz clic en tu servicio web

2. **Ir a la sección de Variables de Entorno**:
   - En el menú lateral, haz clic en **"Environment"**
   - O busca la sección **"Environment Variables"**

3. **Agregar DATABASE_URL**:
   - Haz clic en **"Add Environment Variable"**
   - **Key**: `DATABASE_URL`
   - **Value**: Pega la URL que copiaste en el Paso 1 (la Internal Database URL)
   - Haz clic en **"Save Changes"**

4. **Agregar SECRET_KEY**:
   - Haz clic en **"Add Environment Variable"** nuevamente
   - **Key**: `SECRET_KEY`
   - **Value**: Genera una clave segura (ver sección [Generar SECRET_KEY](#generar-secret_key) más abajo)
   - Haz clic en **"Save Changes"**

### Paso 3: Verificar y Desplegar

1. **Verificar que las variables estén configuradas**:
   - Deberías ver:
     - `DATABASE_URL` = `postgresql://...`
     - `SECRET_KEY` = `tu-clave-secreta`

2. **Re-desplegar la aplicación**:
   - Render detectará los cambios automáticamente
   - O puedes hacer clic en **"Manual Deploy"** → **"Deploy latest commit"**

3. **Verificar los logs**:
   - Ve a la pestaña **"Logs"**
   - Busca mensajes como:
     - ✅ "Creating tables..."
     - ✅ "Database connection successful"
     - ✅ "Application started"

4. **Probar la aplicación**:
   - Ve a tu URL (ej: `https://pixelpick.onrender.com`)
   - Intenta registrarte con un nuevo usuario
   - Si funciona, ¡la base de datos está conectada! 🎉

---

## 2. Railway 🚂

### Paso 1: Crear Base de Datos PostgreSQL

1. **Ve a tu proyecto en Railway**: [railway.app](https://railway.app)

2. **Agregar servicio PostgreSQL**:
   - En tu proyecto, haz clic en **"+ New"**
   - Selecciona **"Database"** → **"Add PostgreSQL"**
   - Railway creará automáticamente una base de datos PostgreSQL

3. **Obtener la URL de conexión**:
   - Haz clic en el servicio de PostgreSQL que acabas de crear
   - Ve a la pestaña **"Variables"**
   - Busca la variable **`DATABASE_URL`** o **`POSTGRES_URL`**
   - Copia el valor (es la URL de conexión)

### Paso 2: Configurar Variables de Entorno

1. **Ir a tu servicio web**:
   - Haz clic en tu servicio de aplicación web (no el de PostgreSQL)

2. **Agregar variables de entorno**:
   - Ve a la pestaña **"Variables"**
   - Railway ya debería tener `DATABASE_URL` configurada automáticamente
   - Si no, haz clic en **"+ New Variable"**:
     - **Name**: `DATABASE_URL`
     - **Value**: Pega la URL del paso anterior

3. **Agregar SECRET_KEY**:
   - Haz clic en **"+ New Variable"**
   - **Name**: `SECRET_KEY`
   - **Value**: Genera una clave segura (ver sección [Generar SECRET_KEY](#generar-secret_key))

### Paso 3: Verificar Despliegue

1. **Railway desplegará automáticamente** cuando detecte cambios
2. **Revisa los logs** en la pestaña "Deployments"
3. **Prueba tu aplicación** en la URL proporcionada por Railway

---

## 3. Heroku 🟣

### Paso 1: Instalar Heroku CLI

Si no lo tienes instalado:

```bash
# macOS
brew tap heroku/brew && brew install heroku

# O descarga desde: https://devcenter.heroku.com/articles/heroku-cli
```

### Paso 2: Iniciar Sesión en Heroku

```bash
heroku login
```

### Paso 3: Crear Aplicación (si no la tienes)

```bash
cd /Users/astridmarquez/Downloads/PixelPick
heroku create pixelpick-tu-nombre
```

### Paso 4: Agregar Base de Datos PostgreSQL

```bash
# Agregar PostgreSQL (plan gratuito)
heroku addons:create heroku-postgresql:mini

# O plan de pago (recomendado para producción)
# heroku addons:create heroku-postgresql:hobby-dev
```

**Nota**: Heroku automáticamente configurará la variable `DATABASE_URL` con la conexión a PostgreSQL.

### Paso 5: Configurar SECRET_KEY

```bash
# Generar una clave secreta segura
python3 -c "import secrets; print(secrets.token_urlsafe(32))"

# Configurar en Heroku (reemplaza TU_CLAVE con la que generaste)
heroku config:set SECRET_KEY=TU_CLAVE_GENERADA
```

### Paso 6: Desplegar

```bash
# Asegúrate de que tu código esté en Git
git add .
git commit -m "Configure PostgreSQL"

# Desplegar a Heroku
git push heroku main
```

### Paso 7: Verificar

```bash
# Ver logs
heroku logs --tail

# Abrir la aplicación
heroku open
```

---

## 🔑 Generar SECRET_KEY

Necesitas una clave secreta segura para las sesiones. Aquí tienes varias formas de generarla:

### Opción 1: Python (Recomendado)

```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

### Opción 2: Python (Alternativa)

```bash
python3 -c "import os; print(os.urandom(32).hex())"
```

### Opción 3: Online (si no tienes Python)

1. Ve a: https://randomkeygen.com/
2. Copia una clave de la sección "CodeIgniter Encryption Keys"
3. Úsala como tu `SECRET_KEY`

### Ejemplo de SECRET_KEY generada:

```
xK9mP2vQ7wR4tY8uI0oP3aS6dF9gH2jK5lM8nQ1rT4vW7xY0zA3bC6dE9fG2hI
```

**⚠️ IMPORTANTE**: 
- Nunca compartas tu `SECRET_KEY`
- Usa una clave diferente para desarrollo y producción
- Guarda la clave en un lugar seguro

---

## ✅ Checklist de Verificación

Antes de considerar que todo está listo, verifica:

- [ ] Base de datos PostgreSQL creada
- [ ] Variable `DATABASE_URL` configurada en tu servicio web
- [ ] Variable `SECRET_KEY` configurada
- [ ] Aplicación desplegada exitosamente
- [ ] Logs muestran "Database connection successful"
- [ ] Puedes registrarte con un nuevo usuario
- [ ] Puedes iniciar sesión
- [ ] Los datos persisten (cierra sesión y vuelve a iniciar)

---

## 🐛 Solución de Problemas

### Error: "could not connect to server"

**Causa**: La URL de la base de datos es incorrecta o la base de datos no está accesible.

**Solución**:
1. Verifica que `DATABASE_URL` esté configurada correctamente
2. Asegúrate de usar la **Internal Database URL** en Render (no la External)
3. Verifica que la base de datos esté activa (no en pausa)

### Error: "relation does not exist"

**Causa**: Las tablas no se han creado en la base de datos.

**Solución**:
1. La aplicación crea las tablas automáticamente al iniciar
2. Si no se crearon, revisa los logs para ver errores
3. Puedes forzar la creación ejecutando en Python:
   ```python
   from app import app, db
   with app.app_context():
       db.create_all()
   ```

### Error: "password authentication failed"

**Causa**: La contraseña en `DATABASE_URL` es incorrecta.

**Solución**:
1. Regenera la contraseña de la base de datos en tu plataforma
2. Actualiza `DATABASE_URL` con la nueva contraseña
3. Re-despliega la aplicación

### La aplicación funciona pero no guarda datos

**Causa**: La aplicación está usando SQLite en lugar de PostgreSQL.

**Solución**:
1. Verifica que `DATABASE_URL` esté configurada
2. Verifica que la URL comience con `postgresql://`
3. Revisa los logs para ver qué base de datos está usando

---

## 📊 Verificar Conexión a Base de Datos

Puedes verificar que la conexión funciona correctamente:

1. **En Render/Railway/Heroku**:
   - Ve a los logs de tu aplicación
   - Busca mensajes de conexión exitosa

2. **Desde tu aplicación**:
   - Intenta registrarte con un nuevo usuario
   - Si funciona, la base de datos está conectada correctamente

3. **Verificar tablas creadas**:
   - En Render: Ve a tu base de datos → Pestaña "Info" → "Connect"
   - En Railway: Usa el cliente PostgreSQL integrado
   - En Heroku: `heroku pg:psql`

---

## 🎉 ¡Listo!

Una vez completados estos pasos, tu aplicación PixelPick estará completamente funcional con PostgreSQL en producción. Los usuarios podrán:

- ✅ Registrarse desde cualquier parte del mundo
- ✅ Iniciar sesión
- ✅ Sus datos se guardarán en la base de datos
- ✅ Ver su perfil con información real
- ✅ Actualizar su información

**¿Necesitas ayuda?** Revisa los logs de tu plataforma o consulta la documentación específica de Render/Railway/Heroku.

