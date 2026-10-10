# Flujo de trabajo del repositorio

Las reglas son **las mismas del [cliente web](https://github.com/nicolasjaramillocely99/MISW4501-202614-Grupo14-Solventa-FrontWeb/blob/main/docs/flujo-de-trabajo.md)**. Lo que cambia aquí son las herramientas, no el proceso.

## Ramas

| Rama | Para qué | Cómo recibe cambios |
|---|---|---|
| `main` | Lo que está desplegado en producción (demo). Protegida. | Solo desde `develop`, por *pull request*, en cada entrega |
| `develop` | Integración del sprint. Protegida. | Solo desde ramas de historia, por *pull request* |
| `<ID de la historia>` | El trabajo de una historia: `HU-M1.1-01`, `HA-S01` | Quien tomó la historia |
| `T1`…`T7` | La base técnica del sprint 1, que no son historias y no llevan puntos | Quien tomó la tarea |

Reglas:

- Toda rama de trabajo se crea **desde `develop`** y vuelve a `develop` por *pull request*. Nunca desde `main`.
- El nombre de la rama es **únicamente el ID de la historia**, sin prefijos ni nombre corto. El ID se toma del [plan por sprints](https://github.com/leonelparrales22/MISW4501-202614-Grupo14-Solventa/wiki/Plan-de-trabajo-por-sprints) y la carpeta destino de [`pantallas.md`](pantallas.md).
- Nadie hace *push* directo a `develop` ni a `main`.

```bash
git switch develop
git pull
git switch -c HU-M1.1-01
```

## Commits

Conventional Commits con el dominio como alcance: `feat(acceso): captura del documento de identidad`, `fix(siniestros): …`, `test(notificaciones): …`, `docs: …`, `chore: …`. Mensajes en español, en infinitivo o sustantivo, sin punto final.

El alcance es el paquete de `features`, no la capa: `feat(acceso)`, no `feat(viewmodel)`.

## Pull requests

1. Actualizar la rama con `develop` antes de abrirlo.
2. El *pull request* apunta a `develop`. Se diligencia la plantilla (`.github/PULL_REQUEST_TEMPLATE.md`): historia, pantallas de Figma y cómo probarlo.
3. Revisión de al menos un compañero.
4. Pipeline `CI · Cliente móvil` en verde: Android Lint, *build* de depuración y pruebas unitarias con cobertura ≥ 70 %.
5. *Squash and merge* a `develop`; la rama de la historia se borra al integrar.
6. Al cierre de cada sprint o entrega, `develop` se integra a `main` por *pull request*; eso es lo que se despliega a producción (demo).

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
| `ci.yml` | *pull request* hacia `develop` o `main`, y *push* a esas ramas | Android Lint → *build* de depuración → pruebas unitarias con Kover (umbral ≥ 70 %) |
| Instrumentadas nocturnas | **Pendiente** | Espresso sobre emulador en GitHub Actions, equivalente al `e2e-nocturno.yml` del cliente web |
| Dependabot | Semanal | Actualizaciones de Gradle y de las acciones de GitHub |

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
