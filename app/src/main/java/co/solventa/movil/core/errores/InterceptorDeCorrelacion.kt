package co.solventa.movil.core.errores

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Pone el identificador de correlación en cada petición y registra cómo
 * terminó (T6).
 *
 * Va **primero** en la cadena de OkHttp, antes del de autenticación y el de
 * errores: así el identificador existe aunque la petición ni siquiera llegue
 * a salir, y la línea de registro cubre también los reintentos y las
 * redirecciones que los interceptores posteriores pudieran provocar.
 *
 * Respeta el identificador que ya venga puesto, siempre que sea válido. Eso
 * permite que una acción del usuario que dispare varias peticiones —subir el
 * documento y luego consultar la verificación, por ejemplo— comparta un solo
 * identificador y se lea como una sola operación de negocio en el backend.
 */
class InterceptorDeCorrelacion(
    private val registro: RegistroDeRed = RegistroDeRed.DeAndroid,
    private val reloj: () -> Long = System::nanoTime,
) : Interceptor {

    override fun intercept(cadena: Interceptor.Chain): Response {
        val original = cadena.request()
        val existente = original.header(Correlacion.ENCABEZADO)
        val id = existente?.takeIf(Correlacion::esValido) ?: Correlacion.nuevo()

        val peticion = original.newBuilder()
            .header(Correlacion.ENCABEZADO, id)
            .build()

        val inicio = reloj()
        try {
            val respuesta = cadena.proceed(peticion)
            registro.peticion(
                metodo = peticion.method,
                ruta = peticion.url.encodedPath,
                codigo = respuesta.code,
                milisegundos = transcurridos(inicio),
                correlacion = id,
            )
            return respuesta
        } catch (fallo: java.io.IOException) {
            // Un fallo de red no deja respuesta ni código, y es justo el caso
            // que más cuesta diagnosticar después: queda registrado con el
            // mismo identificador para poder cruzarlo con el backend.
            registro.fallo(
                metodo = peticion.method,
                ruta = peticion.url.encodedPath,
                causa = fallo.javaClass.simpleName,
                milisegundos = transcurridos(inicio),
                correlacion = id,
            )
            throw fallo
        }
    }

    private fun transcurridos(inicio: Long) = (reloj() - inicio) / 1_000_000
}
