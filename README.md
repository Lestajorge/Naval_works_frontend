# NavalWorks Frontend

Aplicación multiplataforma para la supervisión y gestión de trabajos en un entorno naval. Ofrece una interfaz compartida para Android y escritorio, desarrollada con Kotlin Multiplatform y Compose Multiplatform.

## Funcionalidades

- Inicio de sesión conectado a un servicio backend.
- Panel de control con indicadores de avance y navegación por trabajos, operarios e incidencias.
- Selector de idioma en el engranaje de configuración: español o gallego.
- Búsqueda de trabajos por bloque, código de isométrico o estado.
- Cambio del estado de un trabajo y opción para añadirle operarios desde su tarjeta.
- Al seleccionar un trabajo, el panel derecho muestra el PDF del isométrico y el panel izquierdo agrupa los operarios asignados en montaje/tuberos y soldadura/soldadores.
- Botón **Asignar Nuevo Trabajo** para elegir un isométrico y uno o varios operarios.
- Botón **Añadir nueva incidencia** para crear incidencias; se muestran en tarjetas que pueden eliminarse.
- Visualización del logotipo de NavalWorks en la sección **Operarios en buque**.

> Los indicadores del panel y las incidencias son demostrativos. Las incidencias se crean y eliminan solo en memoria, por lo que no se conservan al cerrar la aplicación ni se sincronizan con el backend. Los trabajos y operarios sí utilizan los endpoints descritos más abajo.

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

La aplicación espera el backend Spring Security del proyecto Naval_Works. Las peticiones autenticadas usan autenticación HTTP Basic con las credenciales del inicio de sesión.

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/bloques` | Comprobar las credenciales y resolver los bloques de los isométricos |
| `GET` | `/api/operarios` | Obtener operarios; la interfaz usa `id`, `nombre`, `apellidos` y `especialista` |
| `GET` | `/api/trabajos` | Obtener trabajos, con `estado`, `isometricoId` y `operarioIds` |
| `POST` | `/api/trabajos` | Crear un trabajo con estado, isométrico y operarios |
| `PUT` | `/api/trabajos/{id}` | Actualizar el estado o la lista de operarios de un trabajo |
| `GET` | `/api/isometricos` | Obtener los isométricos asociados a los trabajos |
| `GET` | `/api/isometricos/{id}/pdf` | Descargar el PDF del isométrico |

El backend debe escuchar en el puerto `8080`. La aplicación de escritorio usa `localhost`; el emulador Android usa `10.0.2.2` para acceder al equipo anfitrión. En un dispositivo Android físico, configura `apiBaseUrl` en `shared/src/androidMain/kotlin/com/works/naval/Platform.android.kt` con la dirección IP del equipo en la red local. Para producción, utiliza HTTPS en lugar de HTTP.

La API de incidencias aún no está conectada: las acciones de añadir y eliminar incidencias solo modifican la lista local de demostración.

## Pruebas

```bash
# Pruebas compartidas para escritorio
./gradlew :shared:jvmTest

# Pruebas host de Android
./gradlew :shared:testAndroidHostTest
```
