package co.solventa.movil.core.errores

import java.security.SecureRandom

/**
 * El identificador que amarra una petición del usuario con los registros y
 * las trazas que deja en el backend (T6).
 *
 * El contrato lo fija `pkg/observability` del repositorio de servicios, y
 * aquí se respeta al pie de la letra: mismo encabezado, mismo alfabeto y
 * misma longitud. Si el móvil envía algo que no encaja, el middleware lo
 * descarta y genera uno nuevo, con lo que se pierde justamente lo que este
 * identificador existe para dar: poder seguir un toque en el teléfono hasta
 * el servicio que lo atendió.
 */
object Correlacion {

    /** `X-Correlation-Id`, tal como lo declara `pkg/observability`. */
    const val ENCABEZADO = "X-Correlation-Id"

    /**
     * El backend rechaza lo que no case con esta expresión, para que nadie
     * meta basura ni valores enormes en sus registros.
     */
    private val VALIDO = Regex("^[A-Za-z0-9._-]{1,64}\$")

    private val aleatorio = SecureRandom()

    /**
     * Un identificador nuevo: 128 bits en hexadecimal, igual que el
     * `newCorrelationID` del backend, para que los dos lados produzcan
     * valores de la misma forma y nadie tenga que adivinar cuál lo generó.
     */
    fun nuevo(): String {
        val bytes = ByteArray(16)
        aleatorio.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun esValido(valor: String): Boolean = VALIDO.matches(valor)
}
