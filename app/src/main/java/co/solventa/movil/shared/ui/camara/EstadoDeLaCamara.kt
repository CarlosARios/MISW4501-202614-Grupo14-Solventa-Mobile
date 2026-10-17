package co.solventa.movil.shared.ui.camara

/**
 * En qué situación está la cámara para la pantalla que la quiere usar.
 *
 * No es un detalle técnico: cada valor lleva a una pantalla distinta, y la
 * captura del documento tiene que seguir siendo usable —aunque degradada—
 * cuando la cámara no está, porque el emulador de las pruebas nocturnas puede
 * arrancar sin ella y porque el usuario puede negar el permiso.
 */
enum class EstadoDeLaCamara {
    /** Aún no se sabe: no se ha preguntado por el permiso. */
    CONSULTANDO,

    /** Falta el permiso y se puede pedir. */
    SIN_PERMISO,

    /**
     * El usuario negó el permiso. Android no deja volver a preguntar desde la
     * aplicación, así que la única salida es la configuración del sistema.
     */
    PERMISO_NEGADO,

    /** El dispositivo no tiene cámara trasera utilizable. */
    SIN_CAMARA,

    /** Hay permiso y hay cámara: se puede mostrar la vista previa. */
    LISTA,
}
