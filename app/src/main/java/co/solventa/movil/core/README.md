# `core` — servicios transversales (singletons)

Todo lo que existe una sola vez en la aplicación y no tiene interfaz visual. Se provee con Hilt desde `core/di/` y lo consumen `features` y `shared`. **`core` no importa de `features`.**

Equivale a `src/app/core` del [cliente web](https://github.com/nicolasjaramillocely99/MISW4501-202614-Grupo14-Solventa-FrontWeb); las carpetas se llaman igual a propósito, para que una decisión tomada en un canal se encuentre en el mismo lugar en el otro.

| Carpeta | Responsabilidad | Vínculo con la arquitectura |
|---|---|---|
| `auth/sesion/` | Sesión del asegurado, tokens del proveedor de identidad (Amazon Cognito User Pool, OIDC) y su almacenamiento cifrado en el dispositivo | HA-S01 · EC-S03. En el móvil hay un solo rol humano: **asegurado** |
| `auth/biometria/` | `BiometricPrompt` del sistema operativo y desbloqueo **local** de la sesión guardada, sin reautenticar contra el proveedor de identidad | HU-M1.1-02. El criterio de éxito es desbloqueo local, no una nueva autenticación completa |
| `auth/interceptores/` | Adjunta el JWT (`Authorization: Bearer`) a las peticiones al API Gateway; ante 401 cierra la sesión y lleva a `acceso/ingreso` | El *authorizer* de Cognito valida el token en el gateway |
| `api/` | Cliente HTTP base (Retrofit sobre OkHttp, URL del gateway desde `BuildConfig`) y **un cliente por microservicio** | Conector «solicitudes de la app» → API Gateway «Facade» |
| `api/originacion/` | Pólizas del asegurado, renovación, endosos, estado de cobro | Servicio de originación |
| `api/siniestros/` | Radicación, consulta, línea de tiempo, soportes, asistencias | Servicio de siniestros |
| `api/notificaciones/` | Registro del token de dispositivo y preferencias de notificación | Servicio de notificaciones. **A diferencia del cliente web, el móvil sí lo consume**: es el canal al que llegan los eventos por *push* |
| `api/auditoria/` | Consulta y revocación de los consentimientos del propio titular | Servicio de auditoría y consentimientos · RN-02 (revocación efectiva ≤ 5 min) |
| `persistencia/` | Base de datos Room: borradores del registro KYC, cola de evidencias y caché de lectura para el modo sin conexión | HA-L02 y el ADR de caché de lectura. El esquema se exporta a `app/schemas` |
| `sincronizacion/` | *Workers* de WorkManager que vacían la cola cuando vuelve la conexión | Pantallas M9c y M10c (degradado y sin conexión) son su evidencia visible |
| `i18n/` | Idioma activo (es-CO y en-US) y formateo de moneda, fecha y número con `NumberFormat` y `DateTimeFormatter` sobre el `Locale` | OBJ 12 · soporte multimoneda (COP, MXN, CLP, PEN) |
| `navegacion/` | El contrato de rutas (`Ruta`) y la barra inferior (Inicio · Pólizas · Siniestros · Perfil). **El `NavHost` no vive aquí**: es la raíz de composición, conoce todos los dominios y `core` no puede importar de `features`, así que está en el paquete raíz como `SolventaNavHost` | Mapa de navegación móvil |
| `errores/` | Manejo uniforme de errores HTTP, identificador de correlación en las peticiones y mensajes al usuario | Observabilidad con trazas correlacionadas · T6 |
| `di/` | Módulos de Hilt que proveen Retrofit, OkHttp, Room, WorkManager y los clientes de `api/` | La aplicación no construye dependencias a mano |

Orden de los interceptores de OkHttp, igual que en el cliente web: **correlación → autenticación → errores HTTP**.
