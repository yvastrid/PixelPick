# 💳 Configurar Stripe para PixelPick

## 📋 Pasos para Configurar Stripe

### Paso 1: Crear Cuenta en Stripe

1. Ve a: https://stripe.com
2. Haz clic en **"Start now"** o **"Sign up"**
3. Completa el registro con tu información
4. Verifica tu email

### Paso 2: Obtener las Claves API

1. Una vez en el Dashboard de Stripe, ve a **"Developers"** → **"API keys"**
2. Verás dos claves:
   - **Publishable key** (empieza con `pk_test_` o `pk_live_`)
   - **Secret key** (empieza con `sk_test_` o `sk_live_`)

**⚠️ IMPORTANTE:**
- **Test mode**: Usa las claves que empiezan con `pk_test_` y `sk_test_` para desarrollo
- **Live mode**: Usa las claves que empiezan con `pk_live_` y `sk_live_` para producción

### Paso 3: Configurar Webhook (Opcional pero Recomendado)

1. En Stripe Dashboard, ve a **"Developers"** → **"Webhooks"**
2. Haz clic en **"Add endpoint"**
3. **Endpoint URL**: `https://pixelpick-akp2.onrender.com/api/stripe-webhook`
4. **Events to send**: Selecciona:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
5. Haz clic en **"Add endpoint"**
6. Copia el **"Signing secret"** (empieza con `whsec_`)

### Paso 4: Configurar Variables en Render

1. Ve a Render Dashboard: https://dashboard.render.com
2. Abre tu servicio web (pixelpick-akp2)
3. Ve a **"Environment"** → **"Environment Variables"**
4. Agrega estas variables:

**Variable 1:**
- **Key**: `STRIPE_PUBLIC_KEY`
- **Value**: `pk_test_xxxxxxxxxxxxx` (tu Publishable key de Stripe)

**Variable 2:**
- **Key**: `STRIPE_SECRET_KEY`
- **Value**: `sk_test_xxxxxxxxxxxxx` (tu Secret key de Stripe)

**Variable 3 (Opcional - solo si configuraste webhook):**
- **Key**: `STRIPE_WEBHOOK_SECRET`
- **Value**: `whsec_xxxxxxxxxxxxx` (tu Webhook signing secret)

### Paso 5: Re-desplegar la Aplicación

1. En Render, haz clic en **"Manual Deploy"** → **"Deploy latest commit"**
2. Espera a que termine el despliegue (2-5 minutos)

---

## 🧪 Probar el Sistema de Pagos

### Tarjetas de Prueba de Stripe

Stripe proporciona tarjetas de prueba para desarrollo:

**Pago Exitoso:**
- **Número**: `4242 4242 4242 4242`
- **Fecha**: Cualquier fecha futura (ej: `12/25`)
- **CVC**: Cualquier 3 dígitos (ej: `123`)
- **Código Postal**: Cualquier código (ej: `12345`)

**Pago Rechazado:**
- **Número**: `4000 0000 0000 0002`

**Fondos Insuficientes:**
- **Número**: `4000 0000 0000 9995`

**Tarjeta Expirada:**
- **Número**: `4000 0000 0000 0069`

### Probar el Flujo Completo

1. **Registrarse o iniciar sesión** en tu aplicación
2. **Hacer clic en "Comprar Plan"** o "Suscríbete ahora"
3. **Completar el formulario de pago** con una tarjeta de prueba
4. **Verificar** que el pago se procese correctamente
5. **Revisar** en Stripe Dashboard que la transacción aparezca

---

## 🔍 Verificar que Funciona

### En Stripe Dashboard

1. Ve a **"Payments"** en Stripe Dashboard
2. Deberías ver las transacciones de prueba
3. Verifica que aparezcan como **"Succeeded"**

### En tu Base de Datos

1. Conecta a tu base de datos PostgreSQL
2. Verifica la tabla `transactions`:
   ```sql
   SELECT * FROM transactions ORDER BY created_at DESC LIMIT 10;
   ```
3. Verifica la tabla `subscriptions`:
   ```sql
   SELECT * FROM subscriptions ORDER BY created_at DESC LIMIT 10;
   ```

---

## ⚠️ Notas Importantes

1. **Modo Test vs Live**:
   - En desarrollo, usa las claves de **test mode**
   - En producción, cambia a las claves de **live mode**

2. **Webhooks**:
   - Los webhooks son importantes para confirmar pagos automáticamente
   - En desarrollo local, puedes usar Stripe CLI para probar webhooks

3. **Seguridad**:
   - **NUNCA** compartas tu Secret Key públicamente
   - **NUNCA** commits las claves en el código
   - Usa siempre variables de entorno

4. **Moneda**:
   - El plan está configurado para MXN (Pesos Mexicanos)
   - Puedes cambiar la moneda en `config.py` si lo necesitas

---

## 🆘 Solución de Problemas

### Error: "Stripe no está configurado"
- **Solución**: Verifica que `STRIPE_SECRET_KEY` esté configurada en Render

### Error: "Invalid API Key"
- **Solución**: Verifica que las claves sean correctas y no tengan espacios extra

### Los pagos no se confirman automáticamente
- **Solución**: Configura el webhook en Stripe y agrega `STRIPE_WEBHOOK_SECRET`

### Error en el checkout
- **Solución**: Verifica que `STRIPE_PUBLIC_KEY` esté configurada correctamente

---

## 📚 Recursos

- **Documentación de Stripe**: https://stripe.com/docs
- **Stripe Testing**: https://stripe.com/docs/testing
- **Stripe Webhooks**: https://stripe.com/docs/webhooks

---

¡Listo! Una vez configurado, los botones "Suscríbete ahora" y "Comprar Plan" funcionarán correctamente. 🎉

