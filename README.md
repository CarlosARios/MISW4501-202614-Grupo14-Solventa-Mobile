# MISW4501-202614-Grupo14-Solventa-Movil

Cliente móvil de **Solventa** (autoservicio del asegurado en movilidad: pólizas, siniestros, asistencia en sitio, notificaciones y renovación), construido en **Kotlin con Jetpack Compose**. Proyecto Final MISW4501 · Grupo 14 · Universidad de los Andes.

Este repositorio es uno de los cuatro componentes de la solución:

| Componente | Repositorio | Tecnología |
|---|---|---|
| Cliente web | [`…-FrontWeb`](https://github.com/nicolasjaramillocely99/MISW4501-202614-Grupo14-Solventa-FrontWeb) | Angular + TypeScript |
| **Cliente móvil (este)** | `MISW4501-202614-Grupo14-Solventa-Movil` | Kotlin + Jetpack Compose |
| Microservicios de negocio | `…-Backend` | Go sobre ECS Fargate |
| Infraestructura | `…-Infra` | Terraform sobre AWS |

En el [modelo de componentes](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Arquitectura_final#modelo-de-componentes) este cliente vive en **LAYER 4 · Clientes** y solo habla con **LAYER 3 · Gateway** (Amazon API Gateway) por HTTPS; la identidad la emite **Amazon Cognito** (OIDC). Nunca accede a un microservicio, a una base de datos ni a la mensajería directamente. Ver [`docs/arquitectura.md`](docs/arquitectura.md).

A diferencia del cliente web, **esta aplicación sí consume el servicio de notificaciones**: es el canal al que llegan los eventos por *push*.

---

## 1. Stack definido por el equipo

Tomado de [Definición de frameworks y ambientes](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/frameworks) y del [sistema de diseño](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Sistema-de-Dise%C3%B1o_Solventa). Las versiones exactas viven en [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

| Necesidad | Herramienta | Versión instalada |
|---|---|---|
| Lenguaje | Kotlin | 2.2.10 |
| Interfaz | Jetpack Compose (BOM) + Material 3 | 2026.02.01 |
| Build | Android Gradle Plugin sobre Gradle | 9.4.1 / 9.6 |
| Navegación | Navigation Compose | 2.10.2 |
| Inyección de dependencias | Hilt (Dagger) con KSP | 2.60.1 |
| Cliente HTTP | Retrofit + OkHttp + kotlinx.serialization | 3.0.0 / 4.12.0 / 1.9.0 |
| Base de datos local | Room | 2.8.5 |
| Trabajo en segundo plano | WorkManager | 2.12.0 |
| Cámara (documento y evidencia) | CameraX | 1.6.2 |
| Biometría | AndroidX Biometric | 1.1.0 |
| Pruebas unitarias | JUnit4 + MockK + Turbine + coroutines-test | 4.13.2 / 1.13.13 / 1.2.0 / 1.10.2 |
| Pruebas de interfaz | Compose UI Test + Espresso | — / 3.7.0 |
| Cobertura | Kover (umbral del proyecto: ≥ 70 %) | 0.9.11 |
| Análisis estático | Android Lint | incluido en AGP |
| Integración continua | GitHub Actions (por *pull request*) | — |
| Dependencias vulnerables | Dependabot | — |

Dispositivos soportados: **`minSdk 26`** (Android 8.0) y `targetSdk 35`. El `compileSdk` es 37 porque `androidx.core:core-ktx:1.19.1` lo exige; eso no cambia el nivel al que la aplicación declara comportarse. Lienzo de referencia del prototipo **390 × 844**. Idiomas: **es-CO** (predeterminado) y **en-US**.

---

## 2. Puesta en marcha

Requisitos: **Android Studio** (el proyecto usa el JDK que trae incluido) y un emulador o dispositivo con **Android 8.0 o superior**.

En Android Studio: abrir la carpeta, esperar el *sync* de Gradle y ejecutar. Desde la terminal en Windows hay que apuntar `JAVA_HOME` al JDK de Android Studio:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
```

| Comando | Qué hace |
|---|---|
| `./gradlew assembleDebug` | Compila la variante local |
| `./gradlew installDebug` | Instala en el emulador o dispositivo conectado |
| `./gradlew lintDebug` | Android Lint |
| `./gradlew testDebugUnitTest` | Pruebas unitarias |
| `./gradlew koverLogDebug` | Imprime el porcentaje de cobertura |
| `./gradlew koverHtmlReportDebug` | Reporte navegable de cobertura |
| `./gradlew testDebugUnitTest koverVerifyDebug` | La compuerta que corre el pipeline |
| `./gradlew assembleStaging` | Compila la variante de experimentos |
| `./gradlew connectedDebugAndroidTest` | Pruebas instrumentadas (requiere emulador encendido) |

El ambiente local apunta a **`http://10.0.2.2:8080`**, que es como el emulador alcanza el `localhost` del PC donde corre el `docker-compose` del repositorio de servicios. Dentro del emulador, `localhost` es el propio dispositivo virtual.

---

## 3. Estructura del repositorio

```
.
├── .github/
│   ├── workflows/
│   │   └── ci.yml                      # PR a develop/main: lint → build → unitarias con cobertura
│   ├── dependabot.yml
│   └── PULL_REQUEST_TEMPLATE.md
├── docs/
│   ├── arquitectura.md                 # Cómo este cliente respeta los modelos de arquitectura
│   ├── pantallas.md                    # Trazabilidad Figma ↔ paquete ↔ HU ↔ microservicio
│   └── flujo-de-trabajo.md             # Ramas, commits, pull requests y Definition of Done
├── app/
│   ├── schemas/                        # Esquemas de Room exportados (versionados, van al commit)
│   └── src/
│       ├── main/
│       │   ├── java/co/solventa/movil/
│       │   │   ├── core/               # Singletons: auth (Cognito y biometría), api (un cliente por
│       │   │   │                       #   microservicio), persistencia (Room), sincronización
│       │   │   │                       #   (WorkManager), i18n, navegación, errores, di (Hilt)
│       │   │   ├── features/           # Un paquete por dominio de negocio
│       │   │   │   ├── acceso/         # registro con KYC (HU-M1.1-01) e ingreso biométrico (HU-M1.1-02)
│       │   │   │   ├── inicio/         # M3
│       │   │   │   ├── originacion/    # pólizas y renovaciones
│       │   │   │   ├── siniestros/     # radicación, seguimiento y asistencias
│       │   │   │   ├── notificaciones/ # centro de notificaciones y preferencias
│       │   │   │   ├── auditoria/      # consentimientos del titular
│       │   │   │   └── perfil/         # M19 y cambio de idioma
│       │   │   ├── shared/             # Componentes de Compose, modelos y utilidades reutilizables
│       │   │   ├── ui/theme/           # Colores, tipografía y forma del sistema de diseño
│       │   │   ├── MainActivity.kt
│       │   │   └── SolventaApp.kt      # @HiltAndroidApp — contenedor raíz de dependencias
│       │   ├── res/values/             # strings.xml en es-CO (predeterminado)
│       │   ├── res/values-en/          # strings.xml en en-US
│       │   └── AndroidManifest.xml
│       ├── test/                       # Pruebas unitarias (JUnit4, MockK, Turbine)
│       └── androidTest/                # Pruebas instrumentadas (Compose UI Test, Espresso)
├── build.gradle.kts · settings.gradle.kts · gradle.properties
└── gradle/libs.versions.toml           # Catálogo único de versiones
```

Cada carpeta principal tiene un `README.md` que explica qué va en ella y cómo se nombra. Las carpetas vacías llevan un `.gitkeep` hasta que entre el primer archivo.

---

## 4. Tomé una historia de usuario: ¿a qué paquete voy?

1. Abre [`docs/pantallas.md`](docs/pantallas.md) y busca el ID de la historia (`HU-M1.1-01`, `M2.4-01`, …). La tabla indica el **paquete**, el **frame de Figma** y la prioridad.
2. Crea la rama desde `develop` con el ID de la historia como único nombre: `HU-M1.1-01`.
3. Trabaja dentro de `features/<dominio>/<flujo>/`. El `README.md` de `features` lista sus pantallas y sus historias.
4. Las llamadas HTTP salen siempre por el cliente del microservicio en `core/api/<servicio>/`. Si la historia necesita un componente visual reutilizable, va en `shared/ui/`.
5. Los textos van en `res/values/strings.xml` y `res/values-en/strings.xml`; los colores y la tipografía salen de `ui/theme/`.
6. Antes del *pull request*: `./gradlew lintDebug assembleDebug testDebugUnitTest koverVerifyDebug` en verde, con cobertura ≥ 70 % en lo que tocaste.

Correspondencia dominio ↔ microservicio ↔ épica:

| Paquete en `features/` | Microservicio (modelo de componentes) | Épica |
|---|---|---|
| `originacion/` | Servicio de originación | Móvil 1 · Pólizas y renovación |
| `siniestros/` | Servicio de siniestros | Móvil 2 · Siniestros y asistencia |
| `notificaciones/` | Servicio de notificaciones | Móvil 3 · Notificaciones y renovación |
| `auditoria/` | Servicio de auditoría y consentimientos | Transversal · RN-02 |
| `acceso/`, `inicio/`, `perfil/` | Amazon Cognito (HA-S01) | Móvil 1 · Identidad del asegurado |

El dominio de socios B2B no existe en este canal: es del portal.

---

## 5. Convenciones

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas, en español, sin tildes ni guiones | `features.siniestros.seguimiento` |
| Pantalla (composable enrutado) | sufijo `Pantalla`, archivo con el mismo nombre | `MisSiniestrosPantalla.kt` |
| Composable no enrutado | nombre descriptivo sin sufijo | `InsigniaEstado.kt` |
| ViewModel | sufijo `ViewModel`, con `@HiltViewModel` | `MisSiniestrosViewModel` |
| Estado de pantalla | un `data class` con sufijo `UiState`, expuesto como `StateFlow` | `MisSiniestrosUiState` |
| Repositorio | interfaz `…Repositorio` y su implementación `…RepositorioImpl` | `SiniestrosRepositorio` |
| Cliente HTTP de un servicio | sufijo `Api`, una interfaz de Retrofit por microservicio | `SiniestrosApi` |
| DTO de red | sufijo `Dto`, con `@Serializable` y `@SerialName` explícito en cada campo | `SiniestroDto` |
| Entidad de Room | sufijo `Entidad` | `BorradorKycEntidad` |
| Rutas de navegación | objetos de `core/navegacion/`, nunca cadenas literales en la pantalla | `Ruta.MisSiniestros` |
| Claves de texto | `<dominio>_<pantalla>_<texto>` en `strings.xml` | `siniestros_lista_titulo` |
| Prueba unitaria | mismo nombre con sufijo `Test`, en `src/test/` | `MisSiniestrosViewModelTest.kt` |
| Ramas | Únicamente el ID de la historia, creada desde `develop` | `HU-M1.1-01`, `HA-S01` |
| Commits | Conventional Commits con el dominio como alcance | `feat(siniestros): lista con estado sin conexión` |

Reglas de dependencia entre capas: `features` → `shared` y `core`; `shared` no depende de nadie; `core` no importa de `features`. Un dominio nunca importa a otro dominio: lo que comparten va a `shared`.

**`@SerialName` explícito en todos los campos de los DTO.** El nombre que viaja por la red lo fija el contrato del API, no el nombre de la propiedad en Kotlin. Escribirlo siempre convierte un cambio de contrato en una línea y no en una búsqueda por todo el proyecto.

Toda pantalla implementa los **estados** que define el prototipo (vacío, error, sin conexión, degradado, confirmación) dentro del mismo composable; las variantes `b`, `c` de Figma no son rutas distintas.

---

## 6. Ambientes

| Ambiente | Tipo de compilación | `API_GATEWAY_URL` | Uso |
|---|---|---|---|
| Local | `debug` | `http://10.0.2.2:8080` | Desarrollo diario contra el Docker Compose del repositorio de servicios |
| Test / CI | — (sin despliegue) | — | Lint, build y unitarias en cada *pull request* |
| Staging | `staging` (sufijo `.staging` en el `applicationId`) | Pendiente | Experimentos de arquitectura; se enciende solo en su ventana |
| Producción (demo) | `release` | Pendiente | Entrega evaluable |

Los valores marcados `PENDIENTE` (URL del API Gateway, user pool y cliente de Cognito) se completan cuando el repositorio de infraestructura los publique. Se leen desde `BuildConfig`, nunca escritos en una pantalla.

---

## 7. Pruebas

Según la [estrategia de pruebas v1.2](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Estrategia-de-Pruebas-v-1.2):

| Nivel | Herramienta | Dónde | Cuándo corre |
|---|---|---|---|
| Unitarias (OBJ 02, ≥ 70 %) | JUnit4 + MockK + Turbine | `app/src/test/` | Cada *pull request* |
| Base de datos local | `room-testing` sobre migraciones y DAO | `app/src/test/` | Cada *pull request* |
| Interfaz y extremo a extremo (OBJ 06) | Compose UI Test + Espresso | `app/src/androidTest/` | Pendiente: ejecución nocturna sobre emulador |
| Accesibilidad (OBJ 13) | Reglas de accesibilidad de Compose UI Test | `app/src/androidTest/` | Con las instrumentadas |
| Internacionalización (OBJ 12) | Revisión de `values/` y `values-en/` y casos manuales | — | Por sprint |

Regla de integración: ningún cambio entra a `develop` ni a `main` con pruebas fallidas.

---

## 8. Referencias

- [Wiki del proyecto](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki)
- [Modelos de arquitectura y patrones](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Arquitectura_final)
- [Decisiones de arquitectura](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Recopilaci%C3%B3n-Decisiones-de-Arquitectura)
- [Definición de frameworks y ambientes](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/frameworks)
- [Estrategia de pruebas v1.2](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Estrategia-de-Pruebas-v-1.2)
- [Sistema de diseño](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Sistema-de-Dise%C3%B1o_Solventa) y [prototipo en Figma](https://www.figma.com/design/ZBPl5l9oXTPlfiOHrzCM2M/Solventa-%E2%80%94-Prototipo-S7-%E2%80%94-Pantallas)
- [Plan de trabajo por sprints](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Plan-de-trabajo-por-sprints)
- [Flujo de trabajo del cliente web](https://github.com/nicolasjaramillocely99/MISW4501-202614-Grupo14-Solventa-FrontWeb/blob/main/docs/flujo-de-trabajo.md), del que este repositorio copia sus reglas

## Equipo

| Nombre | GitHub |
|---|---|
| Carlos A. Ríos P. | CarlosARios |
| Cristhian S. Contreras B. | criscont17 |
| Leonel A. Parrales M. | leonelparrales22 |
| Maycol S. A. Niño | maycolan |
| Nicolás J. Jaramillo C. | nicolasjaramillocely99 |
