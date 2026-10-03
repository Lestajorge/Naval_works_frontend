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

La aplicación espera un backend accesible en `http://localhost:8080` con estos endpoints:

| Método | Ruta | Uso |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Autenticar con `usuario` y `password`; la respuesta debe indicar `"correcto": true` |
| `GET` | `/api/operarios` | Obtener operarios mediante autenticación HTTP Basic; se esperan los campos `nombre` y `apellidos` |

El host debe ser accesible desde el dispositivo o emulador donde se ejecute la aplicación. En un emulador Android, `localhost` apunta al propio emulador, no al equipo que ejecuta el backend; configura una dirección accesible para ese entorno si fuera necesario.

## Pruebas

```bash
# Pruebas compartidas para escritorio
./gradlew :shared:jvmTest

# Pruebas host de Android
./gradlew :shared:testAndroidHostTest
```
