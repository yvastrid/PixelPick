# 📧 Configurar Verificación de Email - PixelPick

## ✅ Funcionalidad Implementada

He implementado un sistema completo de verificación de email que incluye:

- ✅ Envío automático de correo de verificación al registrarse
- ✅ Página de verificación con token
- ✅ Reenvío de correo de verificación
- ✅ Validación: usuarios no pueden iniciar sesión hasta verificar su email
- ✅ Estado de verificación visible en Configuración
- ✅ Tokens expiran después de 24 horas
- ✅ Límite de reenvío: máximo 1 correo por hora

## 🔧 Configuración Necesaria

Para que el envío de emails funcione, necesitas configurar las variables de entorno en Render:

### Opción 1: Gmail (Recomendado para empezar) ⭐

1. **Ve a tu cuenta de Gmail**
2. **Habilita "Contraseñas de aplicaciones"**:
   - Ve a tu cuenta de Google → Seguridad
   - Activa la verificación en 2 pasos (si no la tienes)
   - Ve a "Contraseñas de aplicaciones"
   - Genera una nueva contraseña para "Mail"
   - Copia la contraseña generada (16 caracteres)

3. **En Render, agrega estas variables de entorno**:
   ```
   MAIL_SERVER=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USE_TLS=true
   MAIL_USERNAME=tu-email@gmail.com
   MAIL_PASSWORD=tu-contraseña-de-aplicación-generada
   MAIL_DEFAULT_SENDER=tu-email@gmail.com
   APP_URL=https://pixelpick-akp2.onrender.com
   ```

### Opción 2: SendGrid (Recomendado para producción)

1. **Crea cuenta en SendGrid**: https://sendgrid.com
2. **Crea una API Key** en SendGrid
3. **En Render, agrega estas variables**:
   ```
   MAIL_SERVER=smtp.sendgrid.net
   MAIL_PORT=587
   MAIL_USE_TLS=true
   MAIL_USERNAME=apikey
   MAIL_PASSWORD=tu-api-key-de-sendgrid
   MAIL_DEFAULT_SENDER=tu-email@tudominio.com
   APP_URL=https://pixelpick-akp2.onrender.com
   ```

### Opción 3: Mailgun

1. **Crea cuenta en Mailgun**: https://mailgun.com
2. **Obtén tus credenciales SMTP**
3. **En Render, agrega estas variables**:
   ```
   MAIL_SERVER=smtp.mailgun.org
   MAIL_PORT=587
   MAIL_USE_TLS=true
   MAIL_USERNAME=tu-usuario-de-mailgun
   MAIL_PASSWORD=tu-contraseña-de-mailgun
   MAIL_DEFAULT_SENDER=noreply@tudominio.com
   APP_URL=https://pixelpick-akp2.onrender.com
   ```

## 📝 Variables de Entorno en Render

Ve a tu servicio web en Render → **Environment** → Agrega:

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `MAIL_SERVER` | Servidor SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Puerto SMTP | `587` |
| `MAIL_USE_TLS` | Usar TLS | `true` |
| `MAIL_USERNAME` | Usuario SMTP | `tu-email@gmail.com` |
| `MAIL_PASSWORD` | Contraseña SMTP | `tu-contraseña-de-app` |
| `MAIL_DEFAULT_SENDER` | Email remitente | `tu-email@gmail.com` |
| `APP_URL` | URL de tu aplicación | `https://pixelpick-akp2.onrender.com` |

## 🚀 Pasos para Configurar

### Paso 1: Configurar Gmail (Más Fácil)

1. Ve a https://myaccount.google.com/security
2. Activa "Verificación en 2 pasos" (si no la tienes)
3. Ve a "Contraseñas de aplicaciones"
4. Genera una nueva contraseña para "Mail"
5. Copia la contraseña de 16 caracteres

### Paso 2: Agregar Variables en Render

1. Ve a tu servicio web en Render
2. Ve a **"Environment"** → **"Environment Variables"**
3. Agrega todas las variables de la tabla de arriba
4. Guarda los cambios

### Paso 3: Re-desplegar

Render detectará los cambios y re-desplegará automáticamente.

## ✅ Verificación

Después de configurar:

1. **Registra un nuevo usuario** con un email real
2. **Revisa tu bandeja de entrada** (y spam)
3. **Haz clic en el enlace de verificación**
4. **Intenta iniciar sesión** - debería funcionar

## 🔍 Cómo Funciona

1. **Usuario se registra** → Se genera un token único
2. **Se envía email** con enlace de verificación
3. **Usuario hace clic en el enlace** → Email se verifica
4. **Usuario puede iniciar sesión** → Solo después de verificar

## 🆘 Solución de Problemas

### Los emails no se envían

**Verifica:**
- ✅ Variables de entorno configuradas correctamente
- ✅ `MAIL_PASSWORD` es correcta (contraseña de aplicación, no tu contraseña normal)
- ✅ `MAIL_USERNAME` es correcto
- ✅ Revisa los logs en Render para ver errores

### Error: "Authentication failed"

**Solución:**
- Usa una "Contraseña de aplicación" de Gmail, no tu contraseña normal
- Verifica que `MAIL_USERNAME` y `MAIL_PASSWORD` sean correctos

### Los emails van a spam

**Solución:**
- Normal al principio, especialmente con Gmail
- Los usuarios deben revisar su carpeta de spam
- Para producción, considera usar SendGrid o Mailgun con dominio verificado

## 📧 Template del Email

El email incluye:
- Diseño HTML atractivo
- Botón de verificación
- Enlace alternativo (por si el botón no funciona)
- Información de expiración (24 horas)
- Branding de PixelPick

## 🎉 ¡Listo!

Una vez configuradas las variables de entorno, el sistema de verificación de email funcionará automáticamente. Los usuarios recibirán un correo al registrarse y deberán verificar antes de poder iniciar sesión.

