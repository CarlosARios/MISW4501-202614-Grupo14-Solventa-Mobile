package co.solventa.movil.core.errores

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * T6, lado del cliente: cada petición sale con el identificador de
 * correlación que `pkg/observability` espera, y queda registrada.
 */
class InterceptorDeCorrelacionTest {

    private lateinit var servidor: MockWebServer
    private lateinit var registro: RegistroFalso
    private lateinit var cliente: OkHttpClient

    @Before
    fun antes() {
        servidor = MockWebServer().apply { start() }
        registro = RegistroFalso()
        // Reloj de mentira: dos lecturas por petición, separadas 25 ms.
        var ticks = 0L
        cliente = OkHttpClient.Builder()
            .addInterceptor(
                InterceptorDeCorrelacion(registro) {
                    ticks += 25_000_000
                    ticks
                },
            )
            .build()
    }

    @After
    fun despues() {
        servidor.close()
    }

    private fun pedir(construir: Request.Builder.() -> Unit = {}) =
        cliente.newCall(
            Request.Builder().url(servidor.url("/v1/polizas")).apply(construir).build(),
        ).execute()

    @Test
    fun `agrega el encabezado de correlacion cuando no viene`() {
        servidor.enqueue(MockResponse(code = 200))

        pedir().close()

        val enviado = servidor.takeRequest().headers[Correlacion.ENCABEZADO]
        assertTrue("Debe ir un identificador", enviado != null)
        assertTrue(
            "Debe cumplir el contrato del backend: $enviado",
            Correlacion.esValido(enviado!!),
        )
    }

    @Test
    fun `respeta el identificador que ya trae la peticion`() {
        servidor.enqueue(MockResponse(code = 200))

        pedir { header(Correlacion.ENCABEZADO, "operacion-de-registro-1") }.close()

        assertEquals(
            "operacion-de-registro-1",
            servidor.takeRequest().headers[Correlacion.ENCABEZADO],
        )
    }

    /**
     * Si el valor no cumple el contrato, el backend lo descarta y genera otro,
     * con lo que se perdería el rastro. Más vale reemplazarlo aquí.
     *
     * El caso se prueba con valores ASCII a propósito: OkHttp rechaza por su
     * cuenta cualquier encabezado con caracteres fuera de ASCII, así que esos
     * ni siquiera llegan al interceptor. Lo que sí llega —y el backend
     * descartaría— son los espacios y los símbolos fuera de su alfabeto, o un
     * valor más largo de 64.
     */
    @Test
    fun `reemplaza un identificador con caracteres que el backend rechazaria`() {
        servidor.enqueue(MockResponse(code = 200))

        pedir { header(Correlacion.ENCABEZADO, "con espacios y @simbolos") }.close()

        val enviado = servidor.takeRequest().headers[Correlacion.ENCABEZADO]!!
        assertTrue("Debe reemplazarse: $enviado", Correlacion.esValido(enviado))
    }

    @Test
    fun `reemplaza un identificador demasiado largo`() {
        servidor.enqueue(MockResponse(code = 200))

        pedir { header(Correlacion.ENCABEZADO, "a".repeat(65)) }.close()

        val enviado = servidor.takeRequest().headers[Correlacion.ENCABEZADO]!!
        assertTrue("65 caracteres superan el límite del backend", Correlacion.esValido(enviado))
    }

    @Test
    fun `registra la peticion con su codigo, su duracion y su correlacion`() {
        servidor.enqueue(MockResponse(code = 201))

        pedir { post("".toRequestBody()) }.close()

        val linea = registro.lineas.single()
        assertEquals("POST", linea.metodo)
        assertEquals("/v1/polizas", linea.ruta)
        assertEquals(201, linea.codigo)
        assertEquals(25L, linea.milisegundos)
        assertTrue(Correlacion.esValido(linea.correlacion))
    }

    @Test
    fun `un fallo de red tambien queda registrado con su correlacion`() {
        servidor.close()

        val fallo = runCatching { pedir().close() }.exceptionOrNull()

        assertTrue("Debe propagar el fallo", fallo is IOException)
        val linea = registro.fallos.single()
        assertEquals("/v1/polizas", linea.ruta)
        assertTrue(Correlacion.esValido(linea.correlacion))
    }

    @Test
    fun `dos peticiones no comparten identificador`() {
        servidor.enqueue(MockResponse(code = 200))
        servidor.enqueue(MockResponse(code = 200))

        pedir().close()
        pedir().close()

        val (una, otra) = registro.lineas
        assertTrue("Cada operación lleva el suyo", una.correlacion != otra.correlacion)
    }
}

private class RegistroFalso : RegistroDeRed {

    data class Linea(
        val metodo: String,
        val ruta: String,
        val codigo: Int,
        val milisegundos: Long,
        val correlacion: String,
    )

    data class Fallo(val ruta: String, val causa: String, val correlacion: String)

    val lineas = mutableListOf<Linea>()
    val fallos = mutableListOf<Fallo>()

    override fun peticion(
        metodo: String,
        ruta: String,
        codigo: Int,
        milisegundos: Long,
        correlacion: String,
    ) {
        lineas += Linea(metodo, ruta, codigo, milisegundos, correlacion)
    }

    override fun fallo(
        metodo: String,
        ruta: String,
        causa: String,
        milisegundos: Long,
        correlacion: String,
    ) {
        fallos += Fallo(ruta, causa, correlacion)
    }
}
