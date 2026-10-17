package co.solventa.movil.core.errores

import android.util.Log

/**
 * Dónde van las líneas de registro de la red.
 *
 * Es una interfaz y no una llamada directa a `Log` por dos razones: las
 * pruebas pueden leer lo que se escribió sin un dispositivo, y el día que
 * haya un servicio de telemetría —Crashlytics o el que el equipo elija— se
 * cambia aquí y no en el interceptor.
 */
interface RegistroDeRed {

    fun peticion(
        metodo: String,
        ruta: String,
        codigo: Int,
        milisegundos: Long,
        correlacion: String,
    )

    fun fallo(
        metodo: String,
        ruta: String,
        causa: String,
        milisegundos: Long,
        correlacion: String,
    )

    /**
     * El registro de Android, con una línea por petición.
     *
     * El formato es `clave=valor` separado por espacios, el mismo estilo que
     * usan los registros estructurados del backend: se lee a simple vista en
     * logcat y se filtra sin esfuerzo por `correlacion=`.
     *
     * **No se registran ni el cuerpo ni los encabezados.** Por ahí viajan el
     * token y los datos del documento de identidad, y logcat lo lee cualquier
     * aplicación con permisos de depuración.
     */
    object DeAndroid : RegistroDeRed {

        private const val ETIQUETA = "SolventaRed"

        override fun peticion(
            metodo: String,
            ruta: String,
            codigo: Int,
            milisegundos: Long,
            correlacion: String,
        ) {
            val linea = "metodo=$metodo ruta=$ruta codigo=$codigo ms=$milisegundos " +
                "correlacion=$correlacion"
            if (codigo >= 500) Log.e(ETIQUETA, linea) else Log.i(ETIQUETA, linea)
        }

        override fun fallo(
            metodo: String,
            ruta: String,
            causa: String,
            milisegundos: Long,
            correlacion: String,
        ) {
            Log.e(
                ETIQUETA,
                "metodo=$metodo ruta=$ruta error=$causa ms=$milisegundos " +
                    "correlacion=$correlacion",
            )
        }
    }
}
