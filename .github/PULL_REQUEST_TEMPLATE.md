## Historia

- ID: <!-- HU-M1.1-01 / HA-S01 / T7 -->
- Sprint: <!-- 1 / 2 / 3 -->
- Dominio: <!-- features/siniestros/seguimiento -->

## Pantallas del prototipo

<!-- Enlaces a los frames de Figma tomados de docs/pantallas.md -->

## Qué cambia

<!-- Resumen funcional en dos o tres líneas -->

## Cómo probarlo

<!-- Ruta hasta la pantalla desde M0, datos de prueba, estados que revisar -->

## Lista de verificación

- [ ] Pantalla y sus estados según el frame de Figma (vacío, error, sin conexión, degradado, confirmación)
- [ ] Textos en `values/strings.xml` y `values-en/strings.xml`; moneda, fecha y número según el `Locale`
- [ ] Llamadas HTTP solo por `core/api/`
- [ ] Pantallas autenticadas detrás de la sesión de `core/auth/`
- [ ] Lo que debe funcionar sin conexión escribe en Room y encola en WorkManager
- [ ] Pruebas unitarias nuevas del `ViewModel` y del repositorio; cobertura ≥ 70 % en lo que cambia
- [ ] Contraste AA, foco visible, `contentDescription` y estados que no dependan solo del color
- [ ] `./gradlew lint`, `./gradlew assembleDebug` y `./gradlew testDebugUnitTest koverVerifyDebug` en verde
- [ ] `docs/pantallas.md` actualizado si cambió la ruta o el paquete
- [ ] Si cambió el esquema de Room, el archivo nuevo de `app/schemas` va en el *commit*
