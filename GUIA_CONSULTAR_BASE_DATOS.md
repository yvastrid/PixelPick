# 📊 Guía para Consultar tu Base de Datos PostgreSQL

## 🎯 Opción 1: DBeaver (Recomendado - Gratis y Fácil)

### Paso 1: Descargar DBeaver
1. Ve a: https://dbeaver.io/download/
2. Descarga la versión Community Edition (gratis)
3. Instala la aplicación

### Paso 2: Conectar a tu Base de Datos
1. Abre DBeaver
2. Haz clic en el botón **"Nueva Conexión"** (icono de enchufe) o **File → New → Database Connection**
3. Selecciona **PostgreSQL**
4. Haz clic en **Next**

### Paso 3: Configurar la Conexión
Necesitas obtener tus credenciales de Render:

1. Ve a Render Dashboard: https://dashboard.render.com
2. Ve a tu base de datos PostgreSQL
3. En la pestaña **"Info"**, encontrarás:
   - **Host**: `dpg-d47oinqli9vc738sl140-a.oregon-postgres.render.com` (o similar)
   - **Port**: `5432`
   - **Database**: `pixelpick`
   - **User**: `pixelpick_user` (o el que aparezca)
   - **Password**: Tu contraseña

**⚠️ IMPORTANTE: NO uses la URL completa en DBeaver. Usa los campos individuales.**

**En DBeaver, completa estos campos en la pestaña "Main":**

- **Host**: `dpg-d47oinqli9vc738sl140-a.oregon-postgres.render.com`
  - (Solo el host, sin `postgresql://` ni nada más)
- **Port**: `5432`
- **Database**: `pixelpick`
- **Username**: `pixelpick_user`
- **Password**: Tu contraseña (haz clic en "Save password" si quieres guardarla)

**En la pestaña "SSL" (importante para Render):**
- Marca **"Use SSL"** ✅
- SSL Mode: `require` o `prefer`

5. Haz clic en **"Test Connection"** para verificar
6. Si te pide descargar drivers, haz clic en **"Download"** y espera
7. Si funciona, haz clic en **"Finish"**

**❌ NO uses esto (formato incorrecto):**
```
postgresql://pixelpick_user:password@host/database
```

**✅ Usa esto (campos individuales):**
- Host: `host`
- Port: `5432`
- Database: `database`
- Username: `pixelpick_user`
- Password: `password`

### Paso 4: Explorar tu Base de Datos
- En el panel izquierdo, expande tu conexión
- Verás todas las tablas: `users`, `games`, `user_games`, `user_preferences`
- Haz doble clic en una tabla para ver sus datos
- Puedes ejecutar consultas SQL haciendo clic derecho en la conexión → **SQL Editor → New SQL Script**

---

## 🎯 Opción 2: TablePlus (Mac/Windows - Interfaz Bonita)

### Paso 1: Descargar TablePlus
1. Ve a: https://tableplus.com/
2. Descarga e instala TablePlus

### Paso 2: Conectar
1. Abre TablePlus
2. Haz clic en **"Create a new connection"**
3. Selecciona **PostgreSQL**
4. Ingresa los mismos datos que en DBeaver:
   - **Name**: `PixelPick Production`
   - **Host**: Tu host de Render
   - **Port**: `5432`
   - **User**: Tu usuario
   - **Password**: Tu contraseña
   - **Database**: `pixelpick`
5. Haz clic en **"Test"** y luego **"Connect"**

---

## 🎯 Opción 3: pgAdmin (Herramienta Oficial de PostgreSQL)

### Paso 1: Descargar pgAdmin
1. Ve a: https://www.pgadmin.org/download/
2. Descarga e instala pgAdmin 4

### Paso 2: Conectar
1. Abre pgAdmin
2. Haz clic derecho en **"Servers"** → **"Create"** → **"Server"**
3. En la pestaña **"General"**:
   - **Name**: `PixelPick Production`
4. En la pestaña **"Connection"**:
   - **Host**: Tu host de Render
   - **Port**: `5432`
   - **Database**: `pixelpick`
   - **Username**: Tu usuario
   - **Password**: Tu contraseña
5. Haz clic en **"Save"**

---

## 🎯 Opción 4: Desde la Terminal (psql)

### Paso 1: Instalar PostgreSQL Client
**En macOS:**
```bash
brew install postgresql
```

**En Windows:**
Descarga desde: https://www.postgresql.org/download/windows/

**En Linux:**
```bash
sudo apt-get install postgresql-client
```

### Paso 2: Conectar
1. Ve a Render → Tu base de datos → **"Info"**
2. Copia tu **"External Database URL"** o **"Internal Database URL"**
3. En la terminal, ejecuta:
```bash
psql "postgresql://usuario:contraseña@host:puerto/database"
```

**Ejemplo:**
```bash
psql "postgresql://pixelpick_user:mi_password@dpg-xxxxx-a.oregon-postgres.render.com:5432/pixelpick"
```

### Paso 3: Consultas Básicas
Una vez conectado, puedes ejecutar:

```sql
-- Ver todas las tablas
\dt

-- Ver estructura de una tabla
\d users

-- Ver todos los usuarios
SELECT * FROM users;

-- Contar usuarios
SELECT COUNT(*) FROM users;

-- Ver los últimos 10 usuarios registrados
SELECT id, first_name, last_name, email, created_at 
FROM users 
ORDER BY created_at DESC 
LIMIT 10;

-- Salir de psql
\q
```

---

## 🎯 Opción 5: Crear un Endpoint de Consulta (Solo para Desarrollo)

Si quieres ver los datos directamente desde tu aplicación, puedo crear un endpoint de administración. **⚠️ ADVERTENCIA: Esto es solo para desarrollo, no lo uses en producción sin autenticación adecuada.**

---

## 📋 Consultas SQL Útiles

### Ver todos los usuarios
```sql
SELECT id, first_name, last_name, email, created_at 
FROM users 
ORDER BY created_at DESC;
```

### Ver usuarios con sus juegos
```sql
SELECT 
    u.first_name, 
    u.last_name, 
    u.email,
    g.name as game_name,
    ug.purchased_at
FROM users u
LEFT JOIN user_games ug ON u.id = ug.user_id
LEFT JOIN games g ON ug.game_id = g.id
ORDER BY u.created_at DESC;
```

### Contar registros por tabla
```sql
SELECT 
    'users' as tabla, COUNT(*) as total FROM users
UNION ALL
SELECT 
    'games' as tabla, COUNT(*) as total FROM games
UNION ALL
SELECT 
    'user_games' as tabla, COUNT(*) as total FROM user_games
UNION ALL
SELECT 
    'user_preferences' as tabla, COUNT(*) as total FROM user_preferences;
```

### Ver estructura de todas las tablas
```sql
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;
```

---

## 🔐 Obtener Credenciales de Render

1. Ve a: https://dashboard.render.com
2. Haz clic en tu base de datos PostgreSQL
3. Ve a la pestaña **"Info"**
4. Ahí encontrarás:
   - **Internal Database URL**: Para usar desde Render (más rápido)
   - **External Database URL**: Para usar desde fuera de Render (tu computadora)
   - **Host**, **Port**, **Database**, **User**, **Password**

---

## ⚠️ Notas de Seguridad

1. **Nunca compartas tus credenciales** de base de datos
2. **Usa conexiones seguras** (SSL) cuando sea posible
3. **No ejecutes comandos destructivos** (DROP, DELETE sin WHERE, etc.) sin estar seguro
4. **Haz backups** antes de hacer cambios importantes

---

## 🆘 Solución de Problemas

### Error: "Connection refused"
- Verifica que estés usando la **External Database URL** (no la Internal)
- Verifica que el firewall no esté bloqueando la conexión

### Error: "Authentication failed"
- Verifica que el usuario y contraseña sean correctos
- Asegúrate de copiar la contraseña completa (puede tener caracteres especiales)

### Error: "Database does not exist"
- Verifica el nombre de la base de datos en Render
- Asegúrate de estar usando el nombre correcto (case-sensitive)

---

## 📚 Recursos Adicionales

- **Documentación de PostgreSQL**: https://www.postgresql.org/docs/
- **DBeaver Documentation**: https://dbeaver.com/docs/
- **Render Database Docs**: https://render.com/docs/databases

---

¿Necesitas ayuda con alguna de estas opciones? ¡Avísame!

