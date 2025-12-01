# PixelPick - Aplicación Android

Aplicación Android nativa para PixelPick, plataforma de suscripción de juegos con recomendaciones impulsadas por IA.

## 📋 Requisitos

- Android Studio Hedgehog (2023.1.1) o superior
- JDK 8 o superior
- Android SDK 24 (Android 7.0) o superior
- Gradle 8.1 o superior

## 🚀 Configuración del Proyecto

### 1. Abrir el Proyecto en Android Studio

1. Abre Android Studio
2. Selecciona "Open an Existing Project"
3. Navega a la carpeta `android` dentro del proyecto PixelPick
4. Espera a que Gradle sincronice el proyecto

### 2. Configurar la URL del Backend

La aplicación necesita conectarse a tu servidor Flask. Por defecto está configurada para:
- `https://pixelpick-akp2.onrender.com`

Para cambiar la URL del backend:

1. Abre `app/build.gradle`
2. Busca la línea:
   ```gradle
   buildConfigField "String", "API_BASE_URL", '"https://pixelpick-akp2.onrender.com"'
   ```
3. Cambia la URL por la de tu servidor Flask
4. Sincroniza el proyecto (File → Sync Project with Gradle Files)

### 3. Configurar Permisos de Internet

Los permisos de Internet ya están configurados en `AndroidManifest.xml`. Si necesitas usar HTTP (no HTTPS) en desarrollo local, asegúrate de que `android:usesCleartextTraffic="true"` esté presente.

### 4. Ejecutar la Aplicación

1. Conecta un dispositivo Android o inicia un emulador
2. Haz clic en "Run" (▶️) o presiona `Shift + F10`
3. Selecciona tu dispositivo/emulador
4. Espera a que la aplicación se compile e instale

## 📱 Estructura del Proyecto

```
android/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/pixelpick/app/
│   │       │   ├── data/
│   │       │   │   ├── api/          # Servicios de API (Retrofit)
│   │       │   │   ├── models/       # Modelos de datos
│   │       │   │   └── repository/   # Repositorios de datos
│   │       │   ├── ui/
│   │       │   │   ├── auth/          # Login y Registro
│   │       │   │   ├── main/          # Pantalla principal
│   │       │   │   ├── profile/       # Perfil de usuario
│   │       │   │   ├── settings/      # Configuración
│   │       │   │   └── splash/        # Pantalla de inicio
│   │       │   └── util/              # Utilidades (SessionManager, etc.)
│   │       ├── res/
│   │       │   ├── layout/            # Layouts XML
│   │       │   ├── values/            # Strings, colors, themes
│   │       │   └── menu/              # Menús
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 🔧 Características Implementadas

### ✅ Autenticación
- Registro de nuevos usuarios
- Inicio de sesión
- Cierre de sesión
- Gestión de sesión con SharedPreferences

### ✅ Perfil de Usuario
- Ver perfil completo
- Estadísticas de juegos (completados, jugando)
- Eliminar cuenta

### ✅ Configuración
- Editar nombre y apellido
- Límite de 3 cambios con restricción de 60 días

### ✅ Juegos
- Obtener lista de juegos
- Obtener recomendaciones
- Ver juegos del usuario

## 🎨 Diseño

La aplicación utiliza Material Design con un tema oscuro personalizado que coincide con el diseño web de PixelPick:
- Colores principales: Azul (#00D4FF) y Púrpura (#5B86E5)
- Fondo oscuro (#0A0E27)
- Tipografía moderna y legible

## 🔌 API Backend

La aplicación se comunica con el backend Flask a través de REST API. Asegúrate de que:

1. El servidor Flask esté corriendo y accesible
2. Las rutas API estén disponibles:
   - `/api/register` - Registro
   - `/api/login` - Login
   - `/api/logout` - Logout
   - `/api/profile` - Perfil
   - `/api/games` - Juegos
   - `/api/games/recommendations` - Recomendaciones
   - etc.

## 🐛 Solución de Problemas

### Error de conexión
- Verifica que la URL del backend sea correcta en `build.gradle`
- Asegúrate de que el servidor Flask esté corriendo
- Verifica los permisos de Internet en el dispositivo

### Error de compilación
- Limpia el proyecto: Build → Clean Project
- Reconstruye: Build → Rebuild Project
- Sincroniza Gradle: File → Sync Project with Gradle Files

### La aplicación se cierra al iniciar
- Verifica los logs en Logcat
- Asegúrate de que todas las dependencias estén instaladas
- Verifica que el dispositivo tenga Android 7.0 o superior

## 📦 Dependencias Principales

- **Retrofit 2.9.0** - Cliente HTTP para llamadas API
- **Gson 2.9.0** - Serialización JSON
- **Material Components** - Componentes UI modernos
- **Coroutines** - Programación asíncrona
- **Lifecycle** - ViewModel y LiveData

## 🔐 Seguridad

- Las contraseñas nunca se almacenan localmente
- La sesión se guarda usando SharedPreferences (considera usar EncryptedSharedPreferences en producción)
- Las comunicaciones deben usar HTTPS en producción

## 📝 Próximas Mejoras

- [ ] Implementar RecyclerView para mostrar juegos
- [ ] Agregar funcionalidad de checkout con Stripe
- [ ] Implementar verificación de email
- [ ] Agregar caché local con Room Database
- [ ] Implementar notificaciones push
- [ ] Agregar modo offline

## 📄 Licencia

Este proyecto es de uso personal/propietario.

## 🤝 Contribuir

Para contribuir al proyecto:
1. Crea una rama nueva para tu feature
2. Realiza tus cambios
3. Prueba exhaustivamente
4. Crea un Pull Request

## 📞 Soporte

Para problemas o preguntas, contacta al equipo de desarrollo.

