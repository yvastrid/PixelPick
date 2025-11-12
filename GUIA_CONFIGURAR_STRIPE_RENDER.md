# 💳 Guía Paso a Paso: Configurar Stripe en Render

## 📋 Paso 1: Crear Cuenta en Stripe

### 1.1 Ir a Stripe
1. Abre tu navegador y ve a: **https://stripe.com**
2. Haz clic en **"Start now"** o **"Sign up"** (arriba a la derecha)
3. Completa el registro:
   - Email
   - Contraseña
   - Nombre
   - País
4. Verifica tu email (revisa tu bandeja de entrada)

---

## 📋 Paso 2: Obtener las Claves API de Stripe

### 2.1 Acceder al Dashboard
1. Una vez que inicies sesión, serás redirigido al **Dashboard de Stripe**
2. Si no estás en el dashboard, haz clic en **"Dashboard"** en el menú superior

### 2.2 Ir a API Keys
1. En el menú lateral izquierdo, busca **"Developers"**
2. Haz clic en **"Developers"**
3. En el submenú que aparece, haz clic en **"API keys"**

### 2.3 Ver las Claves
Verás dos secciones:

#### **Publishable key** (Clave Pública)
- Empieza con `pk_test_` (modo prueba) o `pk_live_` (modo producción)
- Esta es la clave que va en `STRIPE_PUBLIC_KEY`
- **Puedes verla directamente** - haz clic en **"Reveal test key"** o **"Reveal live key"**
- **Cópiala** - se verá algo como: `pk_test_51AbCdEfGhIjKlMnOpQrStUvWxYz1234567890...`

#### **Secret key** (Clave Secreta)
- Empieza con `sk_test_` (modo prueba) o `sk_live_` (modo producción)
- Esta es la clave que va en `STRIPE_SECRET_KEY`
- **⚠️ IMPORTANTE**: Haz clic en **"Reveal test key"** o **"Reveal live key"** para verla
- **Cópiala inmediatamente** - se verá algo como: `sk_test_51AbCdEfGhIjKlMnOpQrStUvWxYz1234567890...`
- **Solo se muestra una vez** - guárdala en un lugar seguro

**📝 Nota**: Para desarrollo, usa las claves de **"Test mode"** (modo prueba). Las claves de **"Live mode"** son para producción real.

---

## 📋 Paso 3: Configurar Variables en Render

### 3.1 Ir a Render Dashboard
1. Abre tu navegador y ve a: **https://dashboard.render.com**
2. Inicia sesión en tu cuenta
3. En la lista de servicios, busca y haz clic en tu servicio web **"pixelpick-akp2"** (o el nombre que tenga)

### 3.2 Ir a Environment Variables
1. En el menú de tu servicio, haz clic en la pestaña **"Environment"**
2. Verás una sección llamada **"Environment Variables"**
3. Aquí es donde agregarás las claves de Stripe

### 3.3 Agregar STRIPE_PUBLIC_KEY

1. Haz clic en el botón **"Add Environment Variable"** (o el botón **"+"** o **"Add"**)
2. En el campo **"Key"**, escribe exactamente:
   ```
   STRIPE_PUBLIC_KEY
   ```
   (Sin espacios, todo en mayúsculas, con guiones bajos)

3. En el campo **"Value"**, pega tu **Publishable key** de Stripe:
   ```
   pk_test_51AbCdEfGhIjKlMnOpQrStUvWxYz1234567890...
   ```
   (Pega la clave completa que copiaste de Stripe)

4. Haz clic en **"Save"** o **"Add"**

### 3.4 Agregar STRIPE_SECRET_KEY

1. Haz clic nuevamente en **"Add Environment Variable"**
2. En el campo **"Key"**, escribe exactamente:
   ```
   STRIPE_SECRET_KEY
   ```
   (Sin espacios, todo en mayúsculas, con guiones bajos)

3. En el campo **"Value"**, pega tu **Secret key** de Stripe:
   ```
   sk_test_51AbCdEfGhIjKlMnOpQrStUvWxYz1234567890...
   ```
   (Pega la clave completa que copiaste de Stripe)

4. Haz clic en **"Save"** o **"Add"**

---

## 📋 Paso 4: Verificar las Variables

Después de agregar ambas variables, deberías ver algo así en la lista:

```
STRIPE_PUBLIC_KEY    pk_test_51... (oculta)
STRIPE_SECRET_KEY    sk_test_51... (oculta)
```

**✅ Importante**: Render oculta los valores por seguridad, pero están guardados correctamente.

---

## 📋 Paso 5: Re-desplegar la Aplicación

### 5.1 Desplegar Cambios
1. En Render, ve a la pestaña **"Events"** o **"Manual Deploy"**
2. Haz clic en **"Manual Deploy"** → **"Deploy latest commit"**
3. Espera a que termine el despliegue (2-5 minutos)

### 5.2 Verificar que Funciona
1. Una vez desplegado, ve a tu aplicación: `https://pixelpick-akp2.onrender.com`
2. Inicia sesión
3. Haz clic en **"Comprar Plan"** o **"Suscríbete ahora"**
4. Deberías ver el formulario de checkout con los campos de Stripe

---

## 🧪 Paso 6: Probar con Tarjetas de Prueba

Stripe proporciona tarjetas de prueba para desarrollo:

### Tarjeta de Prueba - Pago Exitoso
- **Número**: `4242 4242 4242 4242`
- **Fecha de expiración**: Cualquier fecha futura (ej: `12/25`)
- **CVC**: Cualquier 3 dígitos (ej: `123`)
- **Código postal**: Cualquier código (ej: `12345`)

### Otras Tarjetas de Prueba
- **Pago rechazado**: `4000 0000 0000 0002`
- **Fondos insuficientes**: `4000 0000 0000 9995`
- **Tarjeta expirada**: `4000 0000 0000 0069`

---

## 🔍 Verificar que Está Configurado Correctamente

### Opción 1: Usar el Endpoint de Health
1. Ve a: `https://pixelpick-akp2.onrender.com/api/health`
2. Deberías ver en la respuesta algo como:
   ```json
   {
     "email_config": {
       ...
     }
   }
   ```
   (Las claves de Stripe no se muestran por seguridad, pero si la app funciona, están configuradas)

### Opción 2: Probar el Checkout
1. Ve a tu aplicación e inicia sesión
2. Haz clic en **"Comprar Plan"**
3. Si ves el formulario de pago con campos de tarjeta, **¡está funcionando!**

---

## ⚠️ Errores Comunes y Soluciones

### Error: "Stripe no está configurado"
**Solución:**
- Verifica que `STRIPE_SECRET_KEY` esté configurada en Render
- Asegúrate de haber re-desplegado después de agregar las variables

### Error: "Invalid API Key"
**Solución:**
- Verifica que copiaste las claves completas (sin espacios al inicio o final)
- Asegúrate de usar las claves de **Test mode** (empiezan con `pk_test_` y `sk_test_`)
- Verifica que no haya caracteres extra o faltantes

### El formulario no carga
**Solución:**
- Verifica que `STRIPE_PUBLIC_KEY` esté configurada
- Revisa la consola del navegador (F12) para ver errores
- Asegúrate de que la clave pública empiece con `pk_test_` o `pk_live_`

### Los pagos no se procesan
**Solución:**
- Verifica que ambas claves estén configuradas
- Revisa los logs de Render para ver errores
- Asegúrate de usar tarjetas de prueba válidas

---

## 📸 Capturas de Pantalla - Dónde Encontrar las Claves

### En Stripe Dashboard:
```
Dashboard → Developers → API keys
```

Verás:
- **Publishable key**: `pk_test_...` (haz clic en "Reveal test key")
- **Secret key**: `sk_test_...` (haz clic en "Reveal test key")

### En Render:
```
Tu Servicio → Environment → Environment Variables
```

Agrega:
- **Key**: `STRIPE_PUBLIC_KEY` → **Value**: `pk_test_...`
- **Key**: `STRIPE_SECRET_KEY` → **Value**: `sk_test_...`

---

## ✅ Checklist Final

Antes de probar, verifica que tengas:

- [ ] Cuenta creada en Stripe
- [ ] `STRIPE_PUBLIC_KEY` agregada en Render (empieza con `pk_test_`)
- [ ] `STRIPE_SECRET_KEY` agregada en Render (empieza con `sk_test_`)
- [ ] Aplicación re-desplegada en Render
- [ ] Probado el checkout con una tarjeta de prueba

---

## 🆘 Si Necesitas Ayuda

Si tienes problemas:

1. **Verifica los logs en Render**:
   - Ve a tu servicio → pestaña **"Logs"**
   - Busca errores relacionados con Stripe

2. **Verifica las variables**:
   - Ve a **Environment** → **Environment Variables**
   - Asegúrate de que ambas claves estén ahí

3. **Prueba localmente primero** (opcional):
   - Crea un archivo `.env` en tu proyecto
   - Agrega: `STRIPE_PUBLIC_KEY=pk_test_...` y `STRIPE_SECRET_KEY=sk_test_...`
   - Ejecuta `python app.py` y prueba localmente

---

## 🎉 ¡Listo!

Una vez configurado, los usuarios podrán:
- ✅ Ver el formulario de pago
- ✅ Ingresar datos de tarjeta
- ✅ Completar el pago
- ✅ Activar su suscripción automáticamente

**¿Necesitas ayuda con algún paso específico?** Avísame y te ayudo.

