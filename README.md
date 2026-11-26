# NavalWorks Frontend

Aplicación multiplataforma para la gestión y supervisión de trabajos en el entorno naval. Está desarrollada con Kotlin Multiplatform y Compose Multiplatform y dispone de interfaces para Android y escritorio.

## Características principales

- Inicio de sesión con credenciales del backend.
- Inicio de sesión adicional como administrador.
- Panel adaptable para móvil, tablet y escritorio.
- Navegación por trabajos, incidencias, operarios e isométricos.
- Cambio de estado y asignación de operarios a trabajos.
- Creación y eliminación de trabajos.
- Creación y eliminación de isométricos con PDF obligatorio.
- Gestión de operarios: alta, modificación y baja.
- Descarga y visualización de PDFs de isométricos.
- Indicadores básicos de avance y estado del taller.
- Interfaz en español y gallego.
- Configuración de URL, usuario y contraseña del servidor.
- Persistencia local de preferencias e incidencias.

## Tecnologías

- Kotlin Multiplatform
- Compose Multiplatform
- Material 3
- Kotlin Coroutines
- Ktor Client
- Kotlin Serialization
- SQLite
- Gradle
- Apache PDFBox en escritorio

## Arquitectura

```text
androidApp/
  Aplicación Android

desktopApp/
  Aplicación de escritorio JVM

shared/
  src/
    commonMain/
      core/             Utilidades comunes y localización
      data/             Acceso al servidor y persistencia de preferencias
      domain/           Modelos y estados del negocio
      platform/         Contratos para capacidades específicas de plataforma
      presentacion/
        app/            Aplicación y panel principal
        configuracion/  Diálogos de acceso y conexión
        incidencias/    Pantallas y diálogos de incidencias
        isometricos/    Administración, tarjetas y visor PDF
        operarios/      Gestión y asignación de operarios
        trabajos/       Lista y creación de trabajos
    androidMain/
      Implementaciones Android de preferencias y capacidades de plataforma
    jvmMain/
      Implementaciones JVM de preferencias y capacidades de plataforma
```

La interfaz y la mayor parte de la lógica residen en el módulo `shared`. Cada pantalla y diálogo se agrupa bajo `presentacion` por el área funcional a la que pertenece; las demás carpetas representan capas técnicas de la aplicación.

Las implementaciones específicas se encuentran en:

- `shared/src/androidMain`: Android.
- `shared/src/jvmMain`: escritorio JVM.

## Requisitos

- JDK 21 recomendado.
- Android Studio para ejecutar la versión Android.
- Backend accesible en el puerto `8080`.
- Para Android físico, el dispositivo debe poder alcanzar la dirección de red del servidor.

## Ejecutar el proyecto

### Escritorio

Desde la raíz del proyecto:

```bash
./gradlew :desktopApp:run
```

También puede generarse una distribución nativa:

```bash
./gradlew :desktopApp:packageDistributionForCurrentOS
```

### Android

Compilar el APK de debug:

```bash
./gradlew :androidApp:assembleDebug
```

El APK se genera en:

```text
androidApp/build/outputs/apk/debug/
```

## Conexión con el backend

La aplicación utiliza autenticación HTTP Basic para las operaciones protegidas.

Las URL predeterminadas son:

- Escritorio: `http://localhost:8080`
- Emulador Android: `http://10.0.2.2:8080`

La URL, el usuario y la contraseña pueden cambiarse desde el panel de configuración de la aplicación.

Para un dispositivo Android físico, la URL predeterminada puede configurarse en:

```text
shared/src/androidMain/kotlin/com/works/naval/platform/Platform.android.kt
```

Por ejemplo:

```kotlin
actual val defaultApiBaseUrl: String = "http://192.168.1.50:8080"
```

En una red real se recomienda HTTPS.

## Endpoints utilizados

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/bloques` | Obtener bloques e iniciar sesión |
| `POST` | `/api/auth/admin` | Validar acceso de administrador |
| `GET` | `/api/operarios` | Obtener operarios |
| `POST` | `/api/operarios` | Crear operario |
| `PUT` | `/api/operarios/{id}` | Modificar operario |
| `DELETE` | `/api/operarios/{id}` | Dar de baja operario |
| `GET` | `/api/trabajos` | Obtener trabajos |
| `POST` | `/api/trabajos` | Crear trabajo |
| `PUT` | `/api/trabajos/{id}` | Actualizar trabajo o estado |
| `DELETE` | `/api/trabajos/{id}` | Eliminar trabajo |
| `GET` | `/api/isometricos` | Obtener isométricos publicados |
| `GET` | `/api/isometricos/admin` | Obtener isométricos para administración |
| `POST` | `/api/isometricos` | Crear isométrico y subir PDF |
| `DELETE` | `/api/isometricos/{id}` | Eliminar isométrico |
| `GET` | `/api/isometricos/{id}/pdf` | Descargar PDF |

El cuerpo de creación de administrador tiene el siguiente formato:

```json
{
  "usuario": "usuario",
  "password": "contraseña"
}
```

La respuesta debe contener el usuario y el rol `ADMINISTRADOR`.

## Incidencias

Las incidencias tienen persistencia local en cada dispositivo.

- Android: SQLite privado de la aplicación.
- Escritorio: `~/.navalworks/preferences.db`.

Al crear o eliminar una incidencia, se actualiza inmediatamente la tabla local de incidencias. La información se conserva al cerrar y volver a abrir la aplicación.

Las incidencias no tienen API propia en el backend. Por tanto:

- Una incidencia creada en una tablet solo aparecerá en esa tablet.
- Una incidencia creada en el escritorio solo aparecerá en ese ordenador.
- Las incidencias no se sincronizan entre dispositivos.
- Las incidencias no se conservan en el backend.

Las incidencias predeterminadas se crean la primera vez que se abre el dashboard. A partir de ese momento, el contenido guardado localmente pasa a ser la fuente utilizada por la aplicación.

## Otros datos locales

Además de las incidencias, en SQLite se guardan:

- Idioma seleccionado.
- URL del servidor.
- Usuario guardado.
- Contraseña guardada.

Los PDFs descargados se almacenan en:

- Android: almacenamiento privado de la aplicación.
- Escritorio: `~/.navalworks/isometricos`.

En Android, los datos locales y los PDFs se eliminan al desinstalar la aplicación.

## Visualización

La interfaz está diseñada para adaptarse a distintos tamaños:

- Móvil.
- Tablet vertical.
- Tablet horizontal.
- Escritorio.

Las vistas principales incluyen:

- Trabajos.
- Asignar nuevo trabajo.
- Incidencias activas.
- Operarios en buque.
- Administrar operarios.
- Gestión de isométricos.
- Detalle y PDF de cada isométrico.

## Estados de trabajo

Los estados disponibles están definidos por el dominio de la aplicación y se muestran con colores propios:

- Aceptado.
- En proceso o estado equivalente enviado por el backend.
- Revisión o terminado, según el valor recibido.

El trabajo conectado al backend puede cambiar de estado desde su tarjeta.

## Pruebas

Pruebas compartidas para escritorio:

```bash
./gradlew :shared:jvmTest
```

Pruebas host de Android:

```bash
./gradlew :shared:testAndroidHostTest
```

Compilar ambos módulos principales:

```bash
./gradlew :desktopApp:build :androidApp:assembleDebug
```

## Estado actual

Implementado:

- Inicio de sesión y cierre de sesión.
- Acceso de administrador.
- Operarios, trabajos e isométricos mediante backend.
- Descarga y almacenamiento local de PDFs.
- Persistencia local de incidencias.
- Gestión local de idioma y configuración de servidor.
- Interfaces responsive para Android y escritorio.

Pendiente o limitado:

- Sincronización de incidencias entre tablet, móvil y escritorio.
- API compartida para incidencias.
- Sincronización de la configuración de incidencias entre dispositivos.
- Indicadores globales actualmente demostrativos o calculados parcialmente.
