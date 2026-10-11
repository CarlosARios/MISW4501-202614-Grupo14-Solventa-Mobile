package co.solventa.movil.features.acceso.registro

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Una prueba por criterio de aceptación de HU-M1.1-01.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repositorio: RepositorioFalso

    @Before
    fun antes() {
        Dispatchers.setMain(dispatcher)
        repositorio = RepositorioFalso()
    }

    @After
    fun despues() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel() = RegistroViewModel(repositorio)

    // Criterio 1 — guiar la captura del frente y del reverso y confirmar los
    // datos extraídos antes de enviarlos a verificación.
    @Test
    fun `capturar las dos caras deja los datos extraidos listos para confirmar`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()

            viewModel.capturar(ImagenDocumento(CaraDocumento.FRENTE, "/tmp/frente.jpg"))
            advanceUntilIdle()
            assertNull("El frente no trae datos del documento", viewModel.estado.value.datos)

            viewModel.capturar(ImagenDocumento(CaraDocumento.REVERSO, "/tmp/reverso.jpg"))
            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertEquals("Carolina", estado.datos?.nombres)
            assertEquals("1.020.345.678", estado.datos?.numeroDocumento)
            assertFalse("Faltan los términos", estado.puedeVerificar)
        }

    // Criterio 4 — la calidad insuficiente se informa durante la captura y el
    // flujo no avanza con esa imagen.
    @Test
    fun `una imagen con poca luz se reporta y no se guarda la captura`() =
        runTest(dispatcher) {
            repositorio.resultadoCaptura =
                ResultadoCaptura.CalidadInsuficiente(ProblemaCalidad.ILUMINACION_INSUFICIENTE)
            val viewModel = crearViewModel()

            viewModel.capturar(ImagenDocumento(CaraDocumento.FRENTE, "/tmp/oscura.jpg"))
            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertEquals(ProblemaCalidad.ILUMINACION_INSUFICIENTE, estado.problemaCalidad)
            assertNull("La captura no debe quedar registrada", estado.frente)

            viewModel.descartarProblemaCalidad()
            assertNull(viewModel.estado.value.problemaCalidad)
        }

    // Criterio 2 — la verificación exitosa activa la cuenta.
    @Test
    fun `la verificacion exitosa activa la cuenta`() = runTest(dispatcher) {
        val viewModel = crearViewModel()
        prepararParaVerificar(viewModel)

        viewModel.verificarIdentidad()
        repositorio.verificacion.complete(ResultadoVerificacion.Exitosa)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(EstadoVerificacion.Exitosa, estado.verificacion)
        assertTrue(estado.cuentaActiva)
    }

    // Criterio 3 — el fallo no revela el motivo técnico; solo una referencia
    // para soporte.
    @Test
    fun `la verificacion fallida entrega una referencia y ningun motivo`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            prepararParaVerificar(viewModel)

            viewModel.verificarIdentidad()
            repositorio.verificacion.complete(
                ResultadoVerificacion.NoSuperada(referencia = "KYC-2026-77A2"),
            )
            advanceUntilIdle()

            val verificacion = viewModel.estado.value.verificacion
            assertTrue(verificacion is EstadoVerificacion.NoSuperada)
            assertEquals("KYC-2026-77A2", (verificacion as EstadoVerificacion.NoSuperada).referencia)
        }

    // Criterio 5 — mientras la verificación no sea exitosa, la cuenta no queda
    // activa en ningún estado intermedio.
    @Test
    fun `la cuenta no queda activa en ningun estado distinto del exito`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            assertFalse(viewModel.estado.value.cuentaActiva)

            prepararParaVerificar(viewModel)
            assertFalse(viewModel.estado.value.cuentaActiva)

            viewModel.verificarIdentidad()
            runCurrent()
            assertEquals(EstadoVerificacion.EnProceso, viewModel.estado.value.verificacion)
            assertFalse(viewModel.estado.value.cuentaActiva)

            repositorio.verificacion.complete(
                ResultadoVerificacion.NoSuperada(referencia = "KYC-2026-0001"),
            )
            advanceUntilIdle()
            assertFalse(viewModel.estado.value.cuentaActiva)
        }

    // Criterio 6 — pasados 30 segundos sin resultado, se le avisa al usuario
    // que lo recibirá por notificación en lugar de dejarlo esperando.
    @Test
    fun `a los treinta segundos sin respuesta el estado pasa a demorada`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            prepararParaVerificar(viewModel)

            viewModel.estado.test {
                assertEquals(EstadoVerificacion.NoIniciada, awaitItem().verificacion)

                viewModel.verificarIdentidad()
                assertEquals(EstadoVerificacion.EnProceso, awaitItem().verificacion)

                advanceTimeBy(RegistroViewModel.LIMITE_DE_ESPERA_MS - 1)
                runCurrent()
                expectNoEvents()

                advanceTimeBy(2)
                runCurrent()
                assertEquals(EstadoVerificacion.Demorada, awaitItem().verificacion)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // Criterio 6 — un resultado que llega después del aviso no le cambia la
    // pantalla al usuario: ya se le dijo que llegaría por notificación.
    @Test
    fun `un resultado tardio no contradice el aviso de notificacion`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            prepararParaVerificar(viewModel)

            viewModel.verificarIdentidad()
            advanceTimeBy(RegistroViewModel.LIMITE_DE_ESPERA_MS + 1)
            runCurrent()
            assertEquals(EstadoVerificacion.Demorada, viewModel.estado.value.verificacion)

            repositorio.verificacion.complete(ResultadoVerificacion.Exitosa)
            advanceUntilIdle()

            assertEquals(EstadoVerificacion.Demorada, viewModel.estado.value.verificacion)
            assertFalse(viewModel.estado.value.cuentaActiva)
        }

    // Criterio 6 — el usuario puede salir de la espera por decisión propia,
    // sin aguantar los 30 segundos.
    @Test
    fun `el usuario puede pedir que le avisen por notificacion antes del limite`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            prepararParaVerificar(viewModel)

            viewModel.verificarIdentidad()
            runCurrent()
            viewModel.avisarPorNotificacion()

            assertEquals(EstadoVerificacion.Demorada, viewModel.estado.value.verificacion)
            assertFalse(viewModel.estado.value.cuentaActiva)
        }

    @Test
    fun `pedir aviso por notificacion no hace nada si no hay verificacion en curso`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()

            viewModel.avisarPorNotificacion()

            assertEquals(EstadoVerificacion.NoIniciada, viewModel.estado.value.verificacion)
        }

    @Test
    fun `sin aceptar los terminos no se puede verificar`() = runTest(dispatcher) {
        val viewModel = crearViewModel()
        capturarAmbasCaras(viewModel)

        assertFalse(viewModel.estado.value.puedeVerificar)

        viewModel.verificarIdentidad()
        advanceUntilIdle()
        assertEquals(EstadoVerificacion.NoIniciada, viewModel.estado.value.verificacion)
    }

    @Test
    fun `reiniciar deja el flujo como al principio`() = runTest(dispatcher) {
        val viewModel = crearViewModel()
        prepararParaVerificar(viewModel)

        viewModel.reiniciar()

        assertEquals(RegistroUiState(), viewModel.estado.value)
    }

    @Test
    fun `corregir un dato del documento reemplaza el extraido`() = runTest(dispatcher) {
        val viewModel = crearViewModel()
        capturarAmbasCaras(viewModel)

        val corregidos = viewModel.estado.value.datos!!.copy(celular = "+57 300 000 0000")
        viewModel.actualizarDatos(corregidos)

        assertEquals("+57 300 000 0000", viewModel.estado.value.datos?.celular)
    }

    private suspend fun kotlinx.coroutines.test.TestScope.capturarAmbasCaras(
        viewModel: RegistroViewModel,
    ) {
        viewModel.capturar(ImagenDocumento(CaraDocumento.FRENTE, "/tmp/frente.jpg"))
        advanceUntilIdle()
        viewModel.capturar(ImagenDocumento(CaraDocumento.REVERSO, "/tmp/reverso.jpg"))
        advanceUntilIdle()
    }

    private suspend fun kotlinx.coroutines.test.TestScope.prepararParaVerificar(
        viewModel: RegistroViewModel,
    ) {
        capturarAmbasCaras(viewModel)
        viewModel.aceptarTerminos(true)
    }
}

/**
 * Doble del repositorio con control explícito del momento en que responde la
 * verificación: es lo que permite probar el límite de 30 segundos sin esperar
 * 30 segundos reales.
 */
private class RepositorioFalso : RegistroKycRepositorio {

    var resultadoCaptura: ResultadoCaptura = ResultadoCaptura.Aceptada(datos = null)
    val verificacion = CompletableDeferred<ResultadoVerificacion>()

    override suspend fun analizarCaptura(imagen: ImagenDocumento): ResultadoCaptura {
        val resultado = resultadoCaptura
        return if (resultado is ResultadoCaptura.Aceptada && imagen.cara == CaraDocumento.REVERSO) {
            ResultadoCaptura.Aceptada(DATOS)
        } else {
            resultado
        }
    }

    override suspend fun verificarIdentidad(datos: DatosDocumento): ResultadoVerificacion =
        verificacion.await()

    private companion object {
        val DATOS = DatosDocumento(
            nombres = "Carolina",
            apellidos = "Pérez Ruiz",
            tipoDocumento = "CC",
            numeroDocumento = "1.020.345.678",
            fechaNacimiento = "14/05/1991",
            correo = "carolina.perez@correo.com",
            celular = "+57 301 234 5678",
        )
    }
}
