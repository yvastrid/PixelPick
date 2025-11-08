# 📧 Guía Paso a Paso: Configurar SendGrid para PixelPick

## 🎯 Objetivo

Configurar SendGrid para que tu aplicación pueda enviar correos de verificación a los usuarios.

---

## 📋 Paso 1: Crear Sender Identity (Identidad del Remitente)

### 1.1 En SendGrid Dashboard

1. En el dashboard de SendGrid, busca la sección **"How to start sending mail"**
2. Haz clic en el botón **"Create sender identity →"**
   - O ve directamente a: **Settings** → **Sender Authentication** → **Single Sender Verification**

### 1.2 Completar el Formulario

Completa los siguientes campos:

- **From Email Address**: `noreply@pixelpick-akp2.onrender.com` 
  - O usa tu email personal: `tu-email@gmail.com` (para pruebas)
- **From Name**: `PixelPick` (o el nombre que prefieras)
- **Reply To**: (opcional) Tu email personal
- **Company Address**: Tu dirección (requerido)
- **City**: Tu ciudad
- **State/Province**: Tu estado/provincia
- **Country**: Tu país
- **Zip Code**: Tu código postal

### 1.3 Verificar el Email

- Si usas tu email personal, SendGrid enviará un correo de verificación
- Revisa tu bandeja de entrada y haz clic en el enlace de verificación
- Si usas un dominio, necesitarás configurar registros DNS (más complejo)

**Para empezar rápido**: Usa tu email personal (ej: `tu-email@gmail.com`)

---

## 📋 Paso 2: Crear API Key

### 2.1 Ir a API Keys

1. En el menú lateral de SendGrid, haz clic en **"Settings"** (Configuración)
2. Haz clic en **"API Keys"**

### 2.2 Crear Nueva API Key

1. Haz clic en el botón **"Create API Key"** (arriba a la derecha)
2. **Name**: `PixelPick Production` (o el nombre que prefieras)
3. **API Key Permissions**: Selecciona **"Full Access"** (para empezar)
   - O **"Restricted Access"** → Marca solo **"Mail Send"**
4. Haz clic en **"Create & View"**

### 2.3 Copiar la API Key

⚠️ **IMPORTANTE**: La API Key solo se muestra UNA VEZ. Cópiala inmediatamente.

Debería verse algo así:
```
SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**Guarda esta clave en un lugar seguro** - la necesitarás en el siguiente paso.

---

## 📋 Paso 3: Configurar Variables en Render

### 3.1 Ir a Render Dashboard

1. Ve a https://dashboard.render.com
2. Haz clic en tu servicio web (pixelpick-akp2)
3. Ve a **"Environment"** → **"Environment Variables"**

### 3.2 Agregar Variables de Email

Haz clic en **"Add Environment Variable"** y agrega cada una:

**Variable 1:**
- **Key**: `MAIL_SERVER`
- **Value**: `smtp.sendgrid.net`

**Variable 2:**
- **Key**: `MAIL_PORT`
- **Value**: `587`

**Variable 3:**
- **Key**: `MAIL_USE_TLS`
- **Value**: `true`

**Variable 4:**
- **Key**: `MAIL_USERNAME`
- **Value**: `apikey`
- (Literalmente la palabra "apikey", no tu username)

**Variable 5:**
- **Key**: `MAIL_PASSWORD`
- **Value**: `SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
- (Pega la API Key que copiaste en el Paso 2.3)

**Variable 6:**
- **Key**: `MAIL_DEFAULT_SENDER`
- **Value**: El email que verificaste en SendGrid (ej: `noreply@pixelpick-akp2.onrender.com` o `tu-email@gmail.com`)

**Variable 7:**
- **Key**: `APP_URL`
- **Value**: `https://pixelpick-akp2.onrender.com`

### 3.3 Guardar Cambios

Haz clic en **"Save Changes"** en cada variable.

---

## 📋 Paso 4: Re-desplegar la Aplicación

1. En Render, haz clic en **"Manual Deploy"** → **"Deploy latest commit"**
2. Espera a que termine el despliegue (2-5 minutos)

---

## ✅ Paso 5: Probar el Sistema

### 5.1 Registrar un Usuario de Prueba

1. Ve a: `https://pixelpick-akp2.onrender.com/login`
2. Regístrate con un email real (el tuyo para probar)
3. Deberías ver el mensaje: "Por favor, verifica tu correo electrónico"

### 5.2 Verificar el Email

1. Revisa tu bandeja de entrada
2. Busca un email de "PixelPick" con el asunto: "Verifica tu correo electrónico - PixelPick"
3. Si no lo ves, revisa la carpeta de **Spam**
4. Haz clic en el botón **"Verificar Mi Correo"** o en el enlace

### 5.3 Iniciar Sesión

1. Después de verificar, ve a: `https://pixelpick-akp2.onrender.com/signin`
2. Inicia sesión con el email y contraseña que usaste
3. Debería funcionar correctamente

---

## 🔍 Verificar que Funciona

### En SendGrid Dashboard

1. Ve a **"Activity"** en el menú lateral
2. Deberías ver los emails enviados
3. Verifica que aparezcan como "Delivered" (Entregado)

### En Render Logs

1. Ve a la pestaña **"Logs"** en Render
2. Busca mensajes como:
   - `"Email de verificación enviado a: ..."`
   - `"Email verificado exitosamente para: ..."`

---

## 🆘 Solución de Problemas

### No recibes el email

1. **Revisa la carpeta de Spam**
2. **Verifica en SendGrid** → **Activity** → ¿Aparece el email?
3. **Revisa los logs en Render** → ¿Hay errores de envío?
4. **Verifica las variables de entorno** → ¿Están todas configuradas?

### Error: "Authentication failed"

**Solución:**
- Verifica que `MAIL_USERNAME` sea exactamente `apikey` (sin comillas)
- Verifica que `MAIL_PASSWORD` sea tu API Key completa (empieza con `SG.`)
- Asegúrate de que la API Key tenga permisos de "Mail Send"

### Error: "Sender identity not verified"

**Solución:**
- Ve a SendGrid → **Settings** → **Sender Authentication**
- Verifica que tu Sender Identity esté verificada (debe tener un check verde)
- Si no está verificada, revisa tu email y haz clic en el enlace de verificación

---

## 📊 Resumen de Variables en Render

Asegúrate de tener estas 7 variables configuradas:

```
MAIL_SERVER=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USE_TLS=true
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.tu-api-key-aqui
MAIL_DEFAULT_SENDER=tu-email-verificado@ejemplo.com
APP_URL=https://pixelpick-akp2.onrender.com
```

---

## 🎉 ¡Listo!

Una vez configurado, cada vez que alguien se registre:
1. ✅ Recibirá un email de verificación automáticamente
2. ✅ Deberá hacer clic en el enlace para verificar
3. ✅ Solo después podrá iniciar sesión

**¿Necesitas ayuda con algún paso específico?** Avísame y te ayudo.

