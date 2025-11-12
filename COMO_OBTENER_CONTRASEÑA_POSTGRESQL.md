# 🔐 Cómo Obtener tu Contraseña de PostgreSQL en Render

## 📋 Paso a Paso

### Paso 1: Ve a tu Base de Datos en Render
1. Abre tu navegador y ve a: **https://dashboard.render.com**
2. Inicia sesión en tu cuenta
3. En el panel izquierdo o en la lista de servicios, busca tu base de datos PostgreSQL
   - Debería llamarse algo como `pixelpick` o similar
   - O busca el servicio que tenga el tipo "PostgreSQL"

### Paso 2: Accede a la Información de la Base de Datos
1. Haz clic en tu base de datos PostgreSQL
2. Verás varias pestañas: **"Info"**, **"Logs"**, **"Settings"**, etc.
3. Haz clic en la pestaña **"Info"**

### Paso 3: Encuentra tu Contraseña
En la pestaña "Info" verás varias secciones. Busca una de estas:

#### Opción A: Campos Individuales
Verás algo como:
```
Host: dpg-d47oinqli9vc738sl140-a.oregon-postgres.render.com
Port: 5432
Database: pixelpick
User: pixelpick_user
Password: [Mostrar] o [Reveal] o [Show]
```

- Haz clic en el botón **"Show"**, **"Reveal"** o **"Mostrar"** junto a Password
- Se mostrará tu contraseña completa

#### Opción B: External Database URL
Si ves una sección llamada **"External Database URL"**, se verá así:
```
postgresql://pixelpick_user:TU_CONTRASEÑA_AQUÍ@dpg-d47oinqli9vc738sl140-a.oregon-postgres.render.com:5432/pixelpick
```

La contraseña está entre `:` y `@`:
- Después de `pixelpick_user:`
- Antes de `@dpg-d47oinqli9vc738sl140-a...`

**Ejemplo:**
```
postgresql://pixelpick_user:KrPn5qyLY6oumvQUW9ZRCHr0T97geBF4@dpg-d47oinqli9vc738sl140-a.oregon-postgres.render.com:5432/pixelpick
```

En este caso, la contraseña sería: `KrPn5qyLY6oumvQUW9ZRCHr0T97geBF4`

### Paso 4: Copia la Contraseña
1. Copia la contraseña completa (puede tener caracteres especiales)
2. Pégala en DBeaver en el campo "Password"
3. **Importante**: Asegúrate de copiar TODA la contraseña, incluyendo cualquier carácter especial

---

## 🔍 Si No Puedes Ver la Contraseña

### Opción 1: Resetear la Contraseña
1. Ve a la pestaña **"Settings"** de tu base de datos en Render
2. Busca la opción **"Reset Password"** o **"Change Password"**
3. Render generará una nueva contraseña
4. **⚠️ IMPORTANTE**: Si cambias la contraseña, también necesitarás actualizar la variable `DATABASE_URL` en tu servicio web de Render

### Opción 2: Usar la Internal Database URL
Si estás conectándote desde Render (no desde tu computadora), puedes usar la **"Internal Database URL"** que no requiere contraseña visible.

---

## 📝 Ejemplo Completo

Basándome en tu error anterior, tu configuración debería ser:

```
Host: dpg-d47oinqli9vc738sl140-a.oregon-postgres.render.com
Port: 5432
Database: pixelpick
Username: pixelpick_user
Password: KrPn5qyLY6oumvQUW9ZRCHr0T97geBF4
```

(Esta contraseña es solo un ejemplo basado en lo que vi en tu error. Debes obtenerla de Render)

---

## ⚠️ Notas Importantes

1. **Nunca compartas tu contraseña** públicamente
2. **Copia la contraseña completa** - puede tener hasta 32 caracteres
3. **Los caracteres especiales** son parte de la contraseña - inclúyelos todos
4. **Si cambias la contraseña**, actualiza también `DATABASE_URL` en tu servicio web

---

## 🆘 Si Aún No Puedes Encontrarla

1. Ve a Render → Tu base de datos → **Settings**
2. Busca **"Reset Password"** o **"Change Password"**
3. Render te mostrará la nueva contraseña
4. Guárdala en un lugar seguro

---

¿Necesitas ayuda con algún paso específico?

