# Flujo de trabajo del repositorio

Las reglas son las del equipo, fijadas en el [`AGENTS.md` del repositorio de servicios](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/blob/develop/AGENTS.md). Lo que cambia aquí son las herramientas, no el proceso.

## Ramas

| Rama | Para qué | Cómo recibe cambios |
|---|---|---|
| `main` | Lo que está desplegado en producción (demo). Protegida. | Solo desde `develop`, por *pull request*, en cada entrega |
| `develop` | Integración del sprint. Protegida. | Solo desde ramas de trabajo, por *pull request* |
| `feature/<ID>-<descripción>` | El trabajo de una historia: `feature/HU-M1.1-01-registro-kyc` | Quien tomó la historia |
| `fix/<ID>-<descripción>` | Corrección sobre una historia ya integrada | Quien la toma |
| `chore/<descripción>` | Trabajo sin historia: la base técnica T1–T7, mantenimiento | Quien la toma |

Reglas:

- Toda rama de trabajo se crea **desde `develop`** y vuelve a `develop` por *pull request*. Nunca desde `main`, salvo un *hotfix*.
- El ID se toma del [plan por sprints](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Plan-de-trabajo-por-sprints) y la carpeta destino de [`pantallas.md`](pantallas.md).
- Nadie hace *push* directo a `develop` ni a `main`.

```bash
git switch develop
git pull
git switch -c feature/HU-M1.1-02-ingreso-biometrico
```

## Commits

Conventional Commits con el dominio como alcance: `feat(acceso): captura del documento de identidad`, `fix(siniestros): …`, `test(notificaciones): …`, `docs: …`, `chore: …`. Mensajes en español, en infinitivo o sustantivo, sin punto final.

El alcance es el paquete de `features`, no la capa: `feat(acceso)`, no `feat(viewmodel)`.

## Pull requests

1. Actualizar la rama con `develop` antes de abrirlo.
2. El *pull request* apunta a `develop`. Se diligencia la plantilla (`.github/PULL_REQUEST_TEMPLATE.md`): historia, pantallas de Figma y cómo probarlo.
3. **El título del *pull request* es el mensaje del commit** tras el *squash*: Conventional Commits con el ID de la historia. Por ejemplo `feat(acceso): registro con verificación de identidad (HU-M1.1-01)`.
4. Revisión de al menos un compañero. Nadie fusiona su propio *pull request* sin revisión.
5. Pipeline `CI · Cliente móvil` en verde: Android Lint, *build* de depuración y pruebas unitarias con cobertura ≥ 70 %.
6. Al fusionar se borra la rama de trabajo. `develop` y `main` no se borran: el *ruleset* lo impide.

### Estrategia de fusión

```
feature/* · fix/* · chore/*  ──(PR, squash)──▶  develop  ──(PR, merge commit)──▶  main
```

| De → A | Estrategia | Por qué |
|---|---|---|
| `feature/*`, `fix/*`, `chore/*` → `develop` | **Squash** | Un commit limpio por historia, fácil de revertir; los intermedios no llegan a `develop` |
| `develop` → `main` | **Merge commit**, nunca *squash* ni *rebase* | Conserva la historia de `develop` y evita que las dos ramas diverjan tras cada entrega |
| *Hotfix* urgente | Rama desde `main` → *pull request* a `main` con **squash**, y después se devuelve el cambio a `develop` | Caso excepcional |

Es la misma estrategia de los otros dos repositorios del proyecto.

## Definition of Done de una historia (cliente móvil)

- [ ] Pantalla construida según el frame de Figma enlazado en [`docs/pantallas.md`](pantallas.md), incluidos sus estados (vacío, error, sin conexión, degradado, confirmación).
- [ ] Textos en `res/values/strings.xml` y `res/values-en/strings.xml`; moneda, fecha y número con `NumberFormat` y `DateTimeFormatter` sobre el `Locale` activo. Ningún texto literal en el código de Compose.
- [ ] Llamadas HTTP solo por el cliente del microservicio en `core/api/`. Ninguna pantalla construye una URL.
- [ ] Las pantallas autenticadas quedan detrás de la sesión de `core/auth/`; 401 lleva a `acceso/ingreso` y 403 a la pantalla de no autorizado.
- [ ] Lo que deba funcionar sin conexión escribe en Room y encola su envío en WorkManager, no se pierde al cerrar la aplicación.
- [ ] Pruebas unitarias del `ViewModel` y del repositorio con MockK y Turbine; cobertura ≥ 70 % en lo nuevo.
- [ ] Contraste AA, foco visible, `contentDescription` en todo elemento no textual y estados que no dependan solo del color (OBJ 13).
- [ ] `./gradlew lint`, `./gradlew assembleDebug` y `./gradlew testDebugUnitTest koverVerifyDebug` en verde.
- [ ] Fila de [`docs/pantallas.md`](pantallas.md) actualizada si cambió la ruta o el paquete.
- [ ] Si la historia toca el esquema de Room, el archivo nuevo de `app/schemas` va en el *commit*.

## Ejecuciones automáticas

| Workflow | Disparo | Qué corre |
|---|---|---|
| `ci.yml` | *pull request* hacia `develop` o `main`, y *push* a esas ramas | Android Lint → *build* de depuración → pruebas unitarias con Kover (umbral ≥ 70 %). En los *push* deja además el **APK instalable** como artefacto |
| `instrumentadas-nocturnas.yml` | Diario a las 02:00 de Bogotá, y bajo demanda | Compose UI Test sobre un emulador con API 34, **fijado en es-CO** |
| `publicar.yml` | Al empujar una etiqueta `v*.*.*` | Verifica, construye y publica un *Release* con el APK adjunto |
| Dependabot | Semanal | Actualizaciones de Gradle y de las acciones de GitHub |

Las ejecuciones programadas solo corren desde la **rama predeterminada** del repositorio: el archivo de las nocturnas tiene que estar en `develop` para que se dispare.

El emulador de las nocturnas arranca con el idioma fijado en **es-CO**, el idioma por omisión de la aplicación. No es un capricho: las pruebas de interfaz comparan textos, y tanto los recursos de la aplicación (`values/` frente a `values-en/`) como las descripciones de accesibilidad de Compose cambian con el idioma del dispositivo. Sin fijarlo, una imagen de sistema distinta cambiaría el resultado sin que nadie hubiera tocado el código.

## Comandos de uso diario

```bash
./gradlew assembleDebug                      # compilar
./gradlew lint                               # analisis estatico de Android
./gradlew testDebugUnitTest                  # pruebas unitarias
./gradlew koverLogDebug                      # ver el porcentaje de cobertura
./gradlew koverHtmlReportDebug               # reporte navegable de cobertura
./gradlew testDebugUnitTest koverVerifyDebug # la compuerta que corre el pipeline
```

Para compilar desde la terminal hay que apuntar `JAVA_HOME` al JDK que trae Android Studio. La ruta cambia según el sistema operativo y según cuántas veces se haya instalado Studio, así que conviene detectarla: el bloque para cada sistema está en el [README, sección 2.3](../README.md#23-compilar-desde-la-terminal).
