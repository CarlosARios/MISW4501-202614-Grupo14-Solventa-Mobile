package co.solventa.movil.features.acceso.ingreso

import co.solventa.movil.core.auth.biometria.AutenticadorBiometrico
import co.solventa.movil.core.auth.biometria.DisponibilidadBiometrica
import co.solventa.movil.core.auth.biometria.ResultadoBiometrico
import co.solventa.movil.core.auth.sesion.ResultadoIngreso
import co.solventa.movil.core.auth.sesion.Sesion
import co.solventa.movil.core.auth.sesion.SesionRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Una prueba por criterio de aceptación de HU-M1.1-02. */
@OptIn(ExperimentalCoroutinesApi::class)
class IngresoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repositorio: RepositorioFalso
    private lateinit var sensor: SensorFalso

    @Before
    fun antes() {
        Dispatchers.setMain(dispatcher)
        repositorio = RepositorioFalso()
        sensor = SensorFalso()
    }

    @After
    fun despues() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel() = IngresoViewModel(repositorio, sensor)

    // Criterio 1 — con biometría disponible y sesión guardada, la pantalla se
    // abre ofreciendo la huella.
    @Test
    fun `con biometria disponible la pantalla abre en modo huella`() = runTest(dispatcher) {
        val viewModel = crearViewModel()

        viewModel.preparar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(MetodoDeIngreso.BIOMETRIA, estado.metodo)
        assertEquals("Carolina", estado.nombre)
        assertFalse(estado.preparando)
    }

    // Criterio 4 — sin biometría configurada en el dispositivo, va directo al
    // formulario de contraseña.
    @Test
    fun `sin biometria configurada abre en el formulario de contrasena`() =
        runTest(dispatcher) {
            sensor.disponibilidad = DisponibilidadBiometrica.NO_CONFIGURADA
            val viewModel = crearViewModel()

            viewModel.preparar()
            advanceUntilIdle()

            assertEquals(MetodoDeIngreso.CONTRASENA, viewModel.estado.value.metodo)
        }

    // Criterio 4 — tampoco se ofrece si el usuario no la habilitó para Solventa.
    @Test
    fun `sin habilitarla para Solventa abre en el formulario de contrasena`() =
        runTest(dispatcher) {
            repositorio.biometria = false
            val viewModel = crearViewModel()

            viewModel.preparar()
            advanceUntilIdle()

            assertEquals(MetodoDeIngreso.CONTRASENA, viewModel.estado.value.metodo)
        }

    // Criterio 2 — el éxito desbloquea la sesión guardada localmente, sin
    // pedirle nada al proveedor de identidad.
    @Test
    fun `la huella correcta desbloquea sin ir al proveedor de identidad`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            viewModel.preparar()
            advanceUntilIdle()

            viewModel.autenticarConHuella()
            advanceUntilIdle()

            assertTrue(viewModel.estado.value.sesionAbierta)
            assertEquals(1, repositorio.vecesDesbloqueado)
            assertEquals(
                "El desbloqueo local no debe llamar al proveedor de identidad",
                0,
                repositorio.vecesAutenticadoConContrasena,
            )
        }

    // Criterio 3 — hasta tres intentos; al tercero la biometría queda pausada
    // y se ofrece la contraseña.
    @Test
    fun `al tercer intento fallido la biometria queda pausada`() = runTest(dispatcher) {
        sensor.resultado = ResultadoBiometrico.NoReconocido
        val viewModel = crearViewModel()
        viewModel.preparar()
        advanceUntilIdle()

        viewModel.autenticarConHuella()
        advanceUntilIdle()
        assertEquals(2, viewModel.estado.value.intentosRestantes)
        assertEquals(MetodoDeIngreso.BIOMETRIA, viewModel.estado.value.metodo)

        viewModel.autenticarConHuella()
        advanceUntilIdle()
        assertEquals(1, viewModel.estado.value.intentosRestantes)
        assertFalse(viewModel.estado.value.biometriaPausada)

        viewModel.autenticarConHuella()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(0, estado.intentosRestantes)
        assertTrue(estado.biometriaPausada)
        assertEquals(MetodoDeIngreso.CONTRASENA, estado.metodo)
        assertFalse(estado.sesionAbierta)
    }

    // Criterio 3 — con la biometría pausada, insistir no cuenta otro intento.
    @Test
    fun `con la biometria pausada no se vuelve a pedir la huella`() = runTest(dispatcher) {
        sensor.resultado = ResultadoBiometrico.NoReconocido
        val viewModel = crearViewModel()
        viewModel.preparar()
        advanceUntilIdle()
        repeat(3) { viewModel.autenticarConHuella(); advanceUntilIdle() }

        val llamadasAntes = sensor.llamadas
        viewModel.autenticarConHuella()
        advanceUntilIdle()

        assertEquals(llamadasAntes, sensor.llamadas)
        assertEquals(3, viewModel.estado.value.intentosFallidos)
    }

    // Cancelar el diálogo no es fallar: el usuario decidió no usarlo.
    @Test
    fun `cancelar el dialogo no gasta un intento`() = runTest(dispatcher) {
        sensor.resultado = ResultadoBiometrico.Cancelado
        val viewModel = crearViewModel()
        viewModel.preparar()
        advanceUntilIdle()

        viewModel.autenticarConHuella()
        advanceUntilIdle()

        assertEquals(0, viewModel.estado.value.intentosFallidos)
        assertEquals(MetodoDeIngreso.BIOMETRIA, viewModel.estado.value.metodo)
    }

    @Test
    fun `si el sistema bloquea el sensor se pasa a contrasena`() = runTest(dispatcher) {
        sensor.resultado = ResultadoBiometrico.NoDisponible
        val viewModel = crearViewModel()
        viewModel.preparar()
        advanceUntilIdle()

        viewModel.autenticarConHuella()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(MetodoDeIngreso.CONTRASENA, estado.metodo)
        assertEquals(ErrorDeIngreso.BiometriaNoDisponible, estado.error)
    }

    @Test
    fun `la contrasena correcta abre la sesion`() = runTest(dispatcher) {
        val viewModel = crearViewModel()
        viewModel.preparar()
        advanceUntilIdle()
        viewModel.cambiarAContrasena()
        viewModel.actualizarCorreo("carolina.perez@correo.com")
        viewModel.actualizarContrasena("Solventa2026")

        viewModel.entrarConContrasena()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.sesionAbierta)
    }

    @Test
    fun `una contrasena de menos de diez caracteres no habilita el boton`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            viewModel.actualizarCorreo("carolina.perez@correo.com")
            viewModel.actualizarContrasena("corta")

            assertFalse(viewModel.estado.value.puedeEntrar)

            viewModel.entrarConContrasena()
            advanceUntilIdle()
            assertEquals(0, repositorio.vecesAutenticadoConContrasena)
        }

    @Test
    fun `las credenciales invalidas no dicen cual de los dos campos fallo`() =
        runTest(dispatcher) {
            val viewModel = crearViewModel()
            viewModel.actualizarCorreo("otro@correo.com")
            viewModel.actualizarContrasena("Solventa2026")

            viewModel.entrarConContrasena()
            advanceUntilIdle()

            assertEquals(ErrorDeIngreso.CredencialesInvalidas, viewModel.estado.value.error)
            assertFalse(viewModel.estado.value.sesionAbierta)
        }

    @Test
    fun `el interruptor de M2b habilita la biometria al entrar`() = runTest(dispatcher) {
        repositorio.biometria = false
        val viewModel = crearViewModel()
        viewModel.preparar()
        advanceUntilIdle()
        viewModel.actualizarCorreo("carolina.perez@correo.com")
        viewModel.actualizarContrasena("Solventa2026")
        viewModel.habilitarBiometriaAlEntrar(true)

        viewModel.entrarConContrasena()
        advanceUntilIdle()

        assertTrue(repositorio.biometria)
    }

    @Test
    fun `la pantalla sabe si se abrio por inactividad`() = runTest(dispatcher) {
        val viewModel = crearViewModel()

        viewModel.preparar(porInactividad = true)
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.porInactividad)
    }
}

private class RepositorioFalso : SesionRepositorio {

    var sesion: Sesion? = Sesion("carolina.perez@correo.com", "Carolina")
    var biometria = true
    var vecesDesbloqueado = 0
    var vecesAutenticadoConContrasena = 0

    override suspend fun sesionGuardada(): Sesion? = sesion

    override suspend fun biometriaHabilitada(): Boolean = biometria

    override suspend fun desbloquear(): Sesion? {
        vecesDesbloqueado++
        return sesion
    }

    override suspend fun ingresarConContrasena(
        correo: String,
        contrasena: String,
    ): ResultadoIngreso {
        vecesAutenticadoConContrasena++
        return if (correo == "carolina.perez@correo.com" && contrasena == "Solventa2026") {
            ResultadoIngreso.Exitoso(Sesion(correo, "Carolina"))
        } else {
            ResultadoIngreso.CredencialesInvalidas
        }
    }

    override suspend fun habilitarBiometria(habilitada: Boolean) {
        biometria = habilitada
    }

    override suspend fun cerrar() {
        sesion = null
    }
}

private class SensorFalso : AutenticadorBiometrico {

    var disponibilidad = DisponibilidadBiometrica.DISPONIBLE
    var resultado: ResultadoBiometrico = ResultadoBiometrico.Exito
    var llamadas = 0

    override fun disponibilidad(): DisponibilidadBiometrica = disponibilidad

    override suspend fun autenticar(): ResultadoBiometrico {
        llamadas++
        return resultado
    }
}
