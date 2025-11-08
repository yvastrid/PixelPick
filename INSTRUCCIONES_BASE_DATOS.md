# Instrucciones para Configurar la Base de Datos - PixelPick

## 📋 Resumen

Tu aplicación PixelPick ahora está completamente funcional con base de datos. Se ha implementado:

- ✅ Sistema de autenticación (registro, login, logout)
- ✅ Gestión de usuarios
- ✅ Perfil de usuario con estadísticas
- ✅ Configuración de cuenta
- ✅ Sistema de juegos y recomendaciones
- ✅ Base de datos compatible con PostgreSQL (producción) y SQLite (desarrollo)

## 🗄️ Estructura de la Base de Datos

La base de datos contiene las siguientes tablas:

1. **users**: Información de usuarios (nombre, apellido, email, contraseña)
2. **games**: Catálogo de juegos disponibles
3. **user_games**: Relación entre usuarios y juegos (qué juegos tiene cada usuario)
4. **user_preferences**: Preferencias del usuario para recomendaciones IA

## 🚀 Configuración Local (Desarrollo)

### Paso 1: Instalar dependencias

```bash
pip install -r requirements.txt
```

### Paso 2: Configurar variables de entorno (opcional)

Crea un archivo `.env` en la raíz del proyecto:

```env
SECRET_KEY=tu-clave-secreta-aqui
```

**Nota**: Si no creas el archivo `.env`, la aplicación usará SQLite automáticamente para desarrollo local.

### Paso 3: Ejecutar la aplicación

```bash
python app.py
```

La aplicación se ejecutará en `http://localhost:8000`

La base de datos SQLite se creará automáticamente en `pixelpick.db` cuando ejecutes la aplicación por primera vez.

## 🌐 Configuración para Producción (Hosting)

Para desplegar en un host (Heroku, Railway, Render, etc.), necesitas configurar PostgreSQL.

### Opción 1: Heroku

1. **Crear aplicación en Heroku**:
   ```bash
   heroku create tu-app-pixelpick
   ```

2. **Agregar base de datos PostgreSQL**:
   ```bash
   heroku addons:create heroku-postgresql:hobby-dev
   ```

3. **Configurar variables de entorno**:
   ```bash
   heroku config:set SECRET_KEY=tu-clave-secreta-muy-segura
   ```

4. **Desplegar**:
   ```bash
   git push heroku main
   ```

Heroku automáticamente configurará la variable `DATABASE_URL` con la conexión a PostgreSQL.

### Opción 2: Railway

1. Conecta tu repositorio de GitHub a Railway
2. Railway detectará automáticamente que es una aplicación Flask
3. Agrega un servicio PostgreSQL desde el panel de Railway
4. Railway automáticamente configurará `DATABASE_URL`
5. Agrega la variable `SECRET_KEY` en las variables de entorno

### Opción 3: Render

1. Crea un nuevo "Web Service" en Render
2. Conecta tu repositorio de GitHub
3. Configura:
   - **Build Command**: `pip install -r requirements.txt`
   - **Start Command**: `gunicorn app:app`
4. Crea una base de datos PostgreSQL en Render
5. Agrega la variable de entorno `SECRET_KEY`
6. Render automáticamente configurará `DATABASE_URL`

### Opción 4: Otra plataforma con PostgreSQL

1. Crea una base de datos PostgreSQL en tu proveedor
2. Obtén la URL de conexión (formato: `postgresql://usuario:contraseña@host:puerto/nombre_db`)
3. Configura la variable de entorno `DATABASE_URL` con esa URL
4. Configura `SECRET_KEY` para sesiones seguras

## 📝 Variables de Entorno Necesarias

- **SECRET_KEY**: Clave secreta para sesiones (genera una aleatoria y segura)
- **DATABASE_URL**: URL de conexión a PostgreSQL (solo para producción)

## 🔧 Funcionalidades Implementadas

### Autenticación
- ✅ Registro de nuevos usuarios (`/api/register`)
- ✅ Inicio de sesión (`/api/login`)
- ✅ Cerrar sesión (`/api/logout`)
- ✅ Protección de rutas con `@login_required`

### Perfil de Usuario
- ✅ Ver perfil completo (`/api/profile`)
- ✅ Actualizar información (`/api/profile/update`)
- ✅ Eliminar cuenta (`/api/profile/delete`)
- ✅ Estadísticas de juegos (completados, jugando)

### Juegos
- ✅ Listar todos los juegos (`/api/games`)
- ✅ Obtener recomendaciones (`/api/games/recommendations`)
- ✅ Agregar juegos al usuario (`/api/user/games`)

## 🧪 Probar la Aplicación

1. **Registro**: Ve a `/login` y crea una cuenta
2. **Inicio de sesión**: Ve a `/signin` e inicia sesión
3. **Perfil**: Ve a `/profile` para ver tu perfil
4. **Configuración**: Ve a `/settings` para editar tu información

## 📦 Dependencias Agregadas

- `Flask-SQLAlchemy`: ORM para base de datos
- `Flask-Login`: Manejo de sesiones de usuario
- `psycopg2-binary`: Driver para PostgreSQL
- `python-dotenv`: Manejo de variables de entorno
- `bcrypt`: Hashing de contraseñas (incluido en Werkzeug)

## 🔒 Seguridad

- Las contraseñas se almacenan con hash (nunca en texto plano)
- Las rutas protegidas requieren autenticación
- Las sesiones están protegidas con `SECRET_KEY`
- Validación de datos en el backend

## 🐛 Solución de Problemas

### Error: "No module named 'flask_sqlalchemy'"
**Solución**: Instala las dependencias: `pip install -r requirements.txt`

### Error de conexión a base de datos
**Solución**: Verifica que `DATABASE_URL` esté configurada correctamente

### La base de datos no se crea
**Solución**: Asegúrate de que la aplicación tenga permisos de escritura en el directorio

### Error en producción con PostgreSQL
**Solución**: Verifica que la URL de PostgreSQL use `postgresql://` (no `postgres://`)

## 📚 Próximos Pasos

Puedes extender la funcionalidad agregando:
- Sistema de búsqueda de juegos
- Sistema de favoritos
- Comentarios y reseñas
- Sistema de recomendaciones IA más avanzado
- Integración con APIs de juegos externas

¡Tu aplicación PixelPick está lista para usar! 🎮

