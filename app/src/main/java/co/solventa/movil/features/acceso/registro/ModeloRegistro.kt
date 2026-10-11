package co.solventa.movil.features.acceso.registro

/** Cara del documento que se está capturando. */
enum class CaraDocumento { FRENTE, REVERSO }

/**
 * Motivo observable por el que una captura no sirve. Es lo único que la
 * aplicación puede explicarle al usuario sobre la imagen, porque son
 * condiciones que él puede corregir (criterio 4).
 */
enum class ProblemaCalidad { ILUMINACION_INSUFICIENTE, DOCUMENTO_INCOMPLETO, IMAGEN_BORROSA }

/** Imagen capturada. La ruta apunta al archivo temporal del dispositivo. */
data class ImagenDocumento(val cara: CaraDocumento, val rutaArchivo: String)

/** Datos que el proveedor extrae del documento y que el usuario confirma. */
data class DatosDocumento(
    val nombres: String,
    val apellidos: String,
    val tipoDocumento: String,
    val numeroDocumento: String,
    val fechaNacimiento: String,
    val correo: String = "",
    val celular: String = "",
)

sealed interface ResultadoCaptura {
    /** La imagen sirve. En el reverso llegan además los datos extraídos. */
    data class Aceptada(val datos: DatosDocumento?) : ResultadoCaptura

    data class CalidadInsuficiente(val motivo: ProblemaCalidad) : ResultadoCaptura
}

/**
 * Resultado de la verificación de identidad.
 *
 * El fallo **no trae motivo**. El criterio 3 exige no revelarle al usuario por
 * qué falló —documento ilegible, identidad no verificable o coincidencia en
 * listas restrictivas son información que no debe salir—, así que el motivo
 * tampoco cruza esta frontera: lo que viaja es una referencia con la que
 * soporte puede rastrear el intento.
 */
sealed interface ResultadoVerificacion {
    data object Exitosa : ResultadoVerificacion
    data class NoSuperada(val referencia: String) : ResultadoVerificacion
}

interface RegistroKycRepositorio {
    suspend fun analizarCaptura(imagen: ImagenDocumento): ResultadoCaptura

    suspend fun verificarIdentidad(datos: DatosDocumento): ResultadoVerificacion
}
