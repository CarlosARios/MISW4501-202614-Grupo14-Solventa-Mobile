# `shared` — lo reutilizable con interfaz visual

Lo que usan varios `features` y no es un servicio transversal. `shared` no conoce ningún dominio: si un componente solo sirve para un flujo, vive en ese flujo.

| Carpeta | Qué va aquí |
|---|---|
| `ui/` | Componentes de Compose del sistema de diseño: botones, campos, tarjetas, avisos, insignias de estado, barra de estado, estados vacío y de error. Toman sus colores y tipografía de `ui/theme` |
| `modelos/` | Tipos de dominio compartidos entre flujos (póliza, siniestro, notificación) y sus enumeraciones de estado |
| `utilidades/` | Funciones de formato, validación de documento, permisos y extensiones de Compose |

Los colores y la tipografía están en [`ui/theme`](../ui/theme), tomados de la guía de estilo del prototipo: azul institucional `#0B3B8C` y lima `#C8E11B`. Los estados **nunca** se comunican solo por color (OBJ 13).
