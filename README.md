# NavalWorks Frontend

Aplicación multiplataforma para la supervisión y gestión de trabajos en un entorno naval. Ofrece una interfaz compartida para Android y escritorio, desarrollada con Kotlin Multiplatform y Compose Multiplatform.

## Funcionalidades

- Inicio de sesión conectado a un servicio backend.
- Panel de control con información de avance de trabajos de la fragata F-110.
- Consulta de trabajos e incidencias activas.
- Consulta de operarios y flujo para asignar trabajos.
- Visualización de documentos PDF e isométricos.

> Algunas pantallas y datos del panel son actualmente demostrativos. La autenticación y la consulta de operarios requieren el backend descrito más abajo.

## Tecnologías

- Kotlin Multiplatform
- Compose Multiplatform y Material 3
- Ktor Client para las peticiones HTTP
- Gradle

## Estructura del proyecto

```text
androidApp/   Aplicación Android
desktopApp/   Aplicación de escritorio (JVM)
shared/       Interfaz y lógica compartidas
```

La lógica común está en `shared/src/commonMain`. El código específico de Android y escritorio se encuentra en `shared/src/androidMain` y `shared/src/jvmMain`, respectivamente.

## Requisitos

- JDK 11 o superior
- Android Studio para compilar y ejecutar la aplicación Android
- Un emulador/dispositivo Android o un entorno de escritorio compatible

## Ejecución

Desde la raíz del proyecto:

```bash
# Compilar la aplicación Android en modo debug
./gradlew :androidApp:assembleDebug

# Ejecutar la aplicación de escritorio
./gradlew :desktopApp:run
```

También puedes abrir el proyecto en Android Studio y ejecutar la configuración de Android desde el IDE.

## Backend

La aplicación espera el backend Spring Security del proyecto Naval_Works:

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/login` | Obtener el formulario y el token CSRF de inicio de sesión |
| `POST` | `/login` | Autenticar con `username`, `password` y el token CSRF; el backend inicia una sesión |
| `GET` | `/api/operarios` | Obtener la lista de operarios usando la sesión iniciada |

El backend debe escuchar en el puerto `8080`. La aplicación de escritorio usa `localhost`; el emulador Android usa `10.0.2.2` para acceder al equipo anfitrión. En un dispositivo Android físico, configura `apiBaseUrl` en `shared/src/androidMain/kotlin/com/works/naval/Platform.android.kt` con la dirección IP del equipo en la red local. Para producción, utiliza HTTPS en lugar de HTTP.

## Pruebas

```bash
# Pruebas compartidas para escritorio
./gradlew :shared:jvmTest

# Pruebas host de Android
./gradlew :shared:testAndroidHostTest
```
