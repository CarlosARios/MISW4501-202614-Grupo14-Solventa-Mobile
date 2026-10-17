package co.solventa.movil.features.acceso.registro

/**
 * Estado de la verificación de identidad.
 *
 * [Demorada] es terminal desde el punto de vista de esta pantalla: una vez que
 * se le dijo al usuario que el resultado le llega por notificación, un cambio
 * posterior contradiría ese mensaje. El resultado tardío se ignora aquí y se
 * entrega por el canal de notificaciones.
 */
sealed interface EstadoVerificacion {
    data object NoIniciada : EstadoVerificacion
    data object EnProceso : EstadoVerificacion
    data object Demorada : EstadoVerificacion
    data object Exitosa : EstadoVerificacion
    data class NoSuperada(val referencia: String) : EstadoVerificacion
}

data class RegistroUiState(
    val frente: ImagenDocumento? = null,
    val reverso: ImagenDocumento? = null,
    val analizando: Boolean = false,
    val problemaCalidad: ProblemaCalidad? = null,
    val datos: DatosDocumento? = null,
    val terminosAceptados: Boolean = false,
    val verificacion: EstadoVerificacion = EstadoVerificacion.NoIniciada,
) {
    /** Habilita el botón de M1c. */
    val puedeVerificar: Boolean
        get() = datos != null &&
            terminosAceptados &&
            verificacion == EstadoVerificacion.NoIniciada

    /**
     * Criterio 5: la cuenta solo queda activa cuando la verificación es
     * exitosa. Mientras tanto no hay acceso a los servicios autenticados.
     */
    val cuentaActiva: Boolean
        get() = verificacion == EstadoVerificacion.Exitosa
}
