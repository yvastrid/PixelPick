# 📱 Guía de Configuración - PixelPick Android

Esta guía te ayudará a configurar y ejecutar la aplicación Android de PixelPick.

## 🚀 Pasos Iniciales

### 1. Instalar Android Studio

1. Descarga Android Studio desde: https://developer.android.com/studio
2. Instala Android Studio siguiendo el asistente
3. Durante la instalación, asegúrate de instalar:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device (AVD)

### 2. Abrir el Proyecto

1. Abre Android Studio
2. Selecciona **"Open"** o **"Open an Existing Project"**
3. Navega a la carpeta `android` dentro del proyecto PixelPick
4. Espera a que Android Studio sincronice el proyecto (esto puede tardar varios minutos la primera vez)

### 3. Configurar el SDK

1. Ve a **File → Project Structure**
2. En **SDK Location**, verifica que la ruta del SDK esté configurada
3. Si no está configurada, haz clic en **"Edit"** y selecciona o descarga el SDK

### 4. Configurar la URL del Backend

**IMPORTANTE:** Debes configurar la URL de tu servidor Flask antes de ejecutar la app.

1. Abre el archivo `app/build.gradle`
2. Busca esta línea (alrededor de la línea 15):
   ```gradle
   buildConfigField "String", "API_BASE_URL", '"https://pixelpick-akp2.onrender.com"'
   ```
3. Cambia la URL por la de tu servidor Flask:
   ```gradle
   buildConfigField "String", "API_BASE_URL", '"https://tu-servidor.com"'
   ```
4. Guarda el archivo
5. Sincroniza el proyecto: **File → Sync Project with Gradle Files**

### 5. Configurar un Dispositivo

Tienes dos opciones:

#### Opción A: Emulador Android (Recomendado para desarrollo)

1. En Android Studio, haz clic en **Device Manager** (ícono de teléfono en la barra lateral)
2. Haz clic en **"Create Device"**
3. Selecciona un dispositivo (recomendado: Pixel 5 o similar)
4. Selecciona una imagen del sistema (recomendado: API 33 o superior)
5. Haz clic en **"Finish"**
6. Espera a que se descargue la imagen del sistema (puede tardar varios minutos)

#### Opción B: Dispositivo Físico

1. Habilita **Opciones de desarrollador** en tu dispositivo Android:
   - Ve a **Configuración → Acerca del teléfono**
   - Toca **Número de compilación** 7 veces
2. Habilita **Depuración USB**:
   - Ve a **Configuración → Opciones de desarrollador**
   - Activa **Depuración USB**
3. Conecta tu dispositivo a la computadora con un cable USB
4. Acepta el diálogo de depuración USB en tu dispositivo

## ▶️ Ejecutar la Aplicación

1. Asegúrate de que tu dispositivo/emulador esté seleccionado en la barra superior
2. Haz clic en el botón **Run** (▶️) o presiona `Shift + F10`
3. Espera a que la aplicación se compile e instale (puede tardar 1-2 minutos la primera vez)
4. La aplicación debería abrirse automáticamente

## 🔧 Solución de Problemas Comunes

### Error: "SDK location not found"

**Solución:**
1. Ve a **File → Project Structure**
2. En **SDK Location**, haz clic en **"Edit"**
3. Selecciona la ruta donde está instalado el Android SDK (normalmente en `~/Library/Android/sdk` en Mac o `C:\Users\TuUsuario\AppData\Local\Android\Sdk` en Windows)

### Error: "Gradle sync failed"

**Solución:**
1. Ve a **File → Invalidate Caches / Restart**
2. Selecciona **"Invalidate and Restart"**
3. Espera a que Android Studio se reinicie y sincronice nuevamente

### Error: "Failed to resolve: com.android.support:appcompat"

**Solución:**
1. Verifica que tengas conexión a Internet
2. Ve a **File → Settings → Appearance & Behavior → System Settings → HTTP Proxy**
3. Asegúrate de que no haya un proxy configurado incorrectamente
4. Sincroniza el proyecto nuevamente

### La aplicación se cierra al iniciar (Crash)

**Solución:**
1. Abre **Logcat** en la parte inferior de Android Studio
2. Busca errores en rojo
3. Los errores más comunes son:
   - **NetworkSecurityConfig**: Si usas HTTP (no HTTPS), asegúrate de que `usesCleartextTraffic="true"` esté en `AndroidManifest.xml` (ya está configurado)
   - **Backend no disponible**: Verifica que tu servidor Flask esté corriendo y accesible

### Error de conexión al backend

**Solución:**
1. Verifica que la URL en `build.gradle` sea correcta
2. Verifica que tu servidor Flask esté corriendo
3. Si usas un emulador y tu servidor está en `localhost`, cambia la URL a `http://10.0.2.2:8000` (para emulador) o usa la IP de tu computadora
4. Verifica los permisos de Internet en `AndroidManifest.xml` (ya están configurados)

## 📝 Notas Importantes

### Autenticación con Flask-Login

La aplicación Android usa cookies de sesión para mantener la autenticación. El `RetrofitClient` está configurado para manejar cookies automáticamente usando `CookieManager`.

### Desarrollo Local

Si estás desarrollando localmente y tu servidor Flask está en `localhost:8000`:

1. Para emulador Android, usa: `http://10.0.2.2:8000`
2. Para dispositivo físico, usa la IP de tu computadora en la red local (ej: `http://192.168.1.100:8000`)

### Producción

En producción, asegúrate de:
- Usar HTTPS (no HTTP)
- Configurar certificados SSL válidos
- Actualizar la URL en `build.gradle` antes de generar el APK de release

## 🎯 Próximos Pasos

Una vez que la aplicación esté funcionando:

1. Prueba el registro de usuarios
2. Prueba el inicio de sesión
3. Explora el perfil y configuración
4. Verifica que los datos se sincronicen correctamente con el backend

## 📞 Ayuda Adicional

Si encuentras problemas que no están cubiertos aquí:
1. Revisa los logs en **Logcat**
2. Verifica la documentación de Android Studio
3. Consulta el README.md principal del proyecto

