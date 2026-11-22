# NavalWorks Frontend

Aplicación multiplataforma para la supervisión y gestión de trabajos en un entorno naval. Ofrece una interfaz compartida para Android y escritorio, desarrollada con Kotlin Multiplatform y Compose Multiplatform.

## Funcionalidades

- Inicio de sesión conectado a un servicio backend.
- Acción **Cerrar sesión** desde el dashboard para volver al inicio de sesión.
- Panel de control con indicadores de avance y navegación por trabajos, operarios e incidencias.
- Selector de idioma en el engranaje de configuración: español o gallego.
- El idioma elegido se guarda localmente en SQLite y se restaura al volver a abrir la aplicación.
- Configuración de conexión para definir la URL, el usuario y la contraseña del servidor, con valores por defecto por plataforma.
- Búsqueda de trabajos por bloque, código de isométrico o estado.
- Cambio del estado de un trabajo y opción para añadirle operarios desde su tarjeta.
- Al seleccionar un trabajo, el panel derecho muestra el PDF del isométrico y el panel izquierdo agrupa los operarios asignados en montaje/tuberos y soldadura/soldadores.
- En **Trabajos**, el botón **Actualizar PDFs** guarda en el dispositivo solo los PDFs de los isométricos asociados a trabajos que aún no estén descargados; muestra el progreso y los fallos. También se pueden guardar o actualizar individualmente desde el visor. Los isométricos guardados aparecen en la lista cuando no hay conexión.
- Botón **Asignar Nuevo Trabajo** para elegir un isométrico y uno o varios operarios.
- Inicio de sesión de administrador desde el menú de configuración y acceso a **Administrar operarios** para dar de alta, dar de baja y modificar operarios en la base de datos.
- Gestión de isométricos para administradores: creación con PDF obligatorio. Todos los isométricos se crean publicados y pueden asignarse a trabajos.
- Usuarios y administradores pueden cambiar estados, asignar operarios, crear trabajos y gestionar incidencias; la gestión de operarios e isométricos requiere el modo administrador.
- Botón **Añadir nueva incidencia** para crear incidencias; se muestran en tarjetas que pueden eliminarse.
- Visualización del logotipo de NavalWorks en la sección **Operarios en buque**.
- Diseño adaptable: navegación compacta por debajo de 840 dp, tarjetas apiladas en móvil y cuadrícula de bloques que ajusta sus columnas al ancho disponible. El dashboard tiene previews de 360×800 (móvil), 800×1280 (tablet vertical) y 1280×800 dp (tablet horizontal).

> Los indicadores del panel y las incidencias son demostrativos. Las incidencias se crean y eliminan solo en memoria, por lo que no se conservan al cerrar la aplicación ni se sincronizan con el backend. Los trabajos y operarios sí utilizan los endpoints descritos más abajo.

## Tecnologías

- Kotlin Multiplatform
- Compose Multiplatform y Material 3
- Ktor Client para las peticiones HTTP
- Gradle

## Estructura del proyecto

```text
androidApp/   Punto de entrada de Android
desktopApp/   Punto de entrada de escritorio (JVM)
shared/
  core/       Funcionalidades base y localización
  data/       Preferencias y comunicación con el backend
  domain/     Modelos y estados del dominio
  platform/   Implementaciones específicas y APIs expect/actual
  presentation/ Pantallas, componentes y diálogos de Compose
```

La lógica compartida está en `shared/src/commonMain`; sus implementaciones específicas de plataforma están en `shared/src/androidMain` y `shared/src/jvmMain`.

Las preferencias de aplicación y conexión se guardan en SQLite: Android utiliza su base de datos privada de la aplicación y escritorio utiliza `~/.navalworks/preferences.db`.

Los PDFs descargados se guardan en el almacenamiento privado de la aplicación en Android y en `~/.navalworks/isometricos` en escritorio. En Android se eliminan al desinstalar la aplicación.

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

La aplicación espera el backend Spring Security del proyecto Naval_Works. Las peticiones autenticadas usan autenticación HTTP Basic con las credenciales del inicio de sesión. El acceso de administrador se valida mediante `POST /api/auth/admin`, enviando `usuario` y `password` en JSON; la respuesta debe incluir el `usuario` y el rol `ADMINISTRADOR`.

| Método | Ruta | Uso |
| --- | --- | --- |
| `GET` | `/api/bloques` | Resolver los bloques de los isométricos |
| `POST` | `/api/auth/admin` | Validar el acceso de administrador con JSON `{"usuario":"...","password":"..."}` |
| `GET` | `/api/operarios` | Obtener operarios; la interfaz usa `id`, `nombre`, `apellidos` y `especialista` |
| `POST` | `/api/operarios` | Crear un operario con `nombre`, `apellidos` y `especialista`; para `Encargado`, enviar además `usuario`, `password` y `administrador: 0` para permitir inicio de sesión sin conceder acceso de administrador |
| `PUT` | `/api/operarios/{id}` | Modificar el nombre, apellidos y especialidad de un operario |
| `DELETE` | `/api/operarios/{id}` | Dar de baja un operario |
| `GET` | `/api/trabajos` | Obtener trabajos, con `estado`, `isometricoId` y `operarioIds` |
| `POST` | `/api/trabajos` | Crear un trabajo con estado, isométrico y operarios |
| `PUT` | `/api/trabajos/{id}` | Actualizar el estado o la lista de operarios de un trabajo |
| `GET` | `/api/isometricos` | Obtener los isométricos asociados a los trabajos |
| `GET` | `/api/isometricos/{id}/pdf` | Descargar el PDF del isométrico |
| `GET` | `/api/isometricos/admin` | Listar todos los isométricos con su estado |
| `POST` | `/api/isometricos` | Crear el isométrico y subir su PDF en una petición multipart con `codigoIsometrico`, `bloqueId` y el archivo en el campo `file` |

El backend debe escuchar en el puerto `8080`. La aplicación de escritorio usa `localhost`; el emulador Android usa `10.0.2.2` para acceder al equipo anfitrión. En un dispositivo Android físico, configura `apiBaseUrl` en `shared/src/androidMain/kotlin/com/works/naval/Platform.android.kt` con la dirección IP del equipo en la red local. Para producción, utiliza HTTPS en lugar de HTTP.

La API de incidencias aún no está conectada: las acciones de añadir y eliminar incidencias solo modifican la lista local de demostración.

El único estado de isométrico es `PUBLICADO`; la creación requiere el PDF y todos los isométricos quedan disponibles para asignación. El backend protege las rutas de administración para el rol `ADMINISTRADOR` y devuelve en `GET /api/isometricos` los isométricos publicados a los encargados.

## Pruebas

```bash
# Pruebas compartidas para escritorio
./gradlew :shared:jvmTest

# Pruebas host de Android
./gradlew :shared:testAndroidHostTest
```
