package co.solventa.movil.core.auth.sesion

import co.solventa.movil.core.auth.biometria.AutenticadorBiometrico
import co.solventa.movil.core.auth.biometria.DisponibilidadBiometrica
import co.solventa.movil.core.auth.biometria.ResultadoBiometrico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Criterio 5 de HU-M1.1-02, visto desde la pantalla: qué pasa cuando la
 * sesión expira y cómo se vuelve.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val limite = ControlDeInactividad.LIMITE_DE_INACTIVIDAD_MS
    private lateinit var sensor: SensorFalso

    @Before
    fun antes() {
        Dispatchers.setMain(dispatcher)
        sensor = SensorFalso()
    }

    @After
    fun despues() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tras cinco minutos quietos la sesion se marca expirada`() = runTest(dispatcher) {
        val control = ControlDeInactividad(this)
        val viewModel = SesionViewModel(control, sensor)

        viewModel.iniciarVigilancia()
        advanceTimeBy(limite + 1)
        runCurrent()

        assertTrue(viewModel.expirada.value)
        control.detener()
    }

    @Test
    fun `tocar la pantalla evita que expire`() = runTest(dispatcher) {
        val control = ControlDeInactividad(this)
        val viewModel = SesionViewModel(control, sensor)
        viewModel.iniciarVigilancia()

        repeat(4) {
            advanceTimeBy(limite / 2)
            runCurrent()
            viewModel.registrarActividad()
        }
        advanceTimeBy(limite / 2)
        runCurrent()

        assertFalse(viewModel.expirada.value)
        control.detener()
    }

    @Test
    fun `la huella correcta devuelve la sesion sin volver a entrar`() = runTest(dispatcher) {
        val control = ControlDeInactividad(this)
        val viewModel = SesionViewModel(control, sensor)
        viewModel.iniciarVigilancia()
        advanceTimeBy(limite + 1)
        runCurrent()
        assertTrue(viewModel.expirada.value)

        viewModel.reautenticarConHuella()
        runCurrent()

        assertFalse(viewModel.expirada.value)
        control.detener()
    }

    @Test
    fun `una huella que no se reconoce deja la hoja puesta`() = runTest(dispatcher) {
        sensor.resultado = ResultadoBiometrico.NoReconocido
        val control = ControlDeInactividad(this)
        val viewModel = SesionViewModel(control, sensor)
        viewModel.iniciarVigilancia()
        advanceTimeBy(limite + 1)
        runCurrent()

        viewModel.reautenticarConHuella()
        runCurrent()

        assertTrue(viewModel.expirada.value)
        control.detener()
    }

    @Test
    fun `no se lanzan dos verificaciones a la vez`() = runTest(dispatcher) {
        val control = ControlDeInactividad(this)
        val viewModel = SesionViewModel(control, sensor)
        viewModel.iniciarVigilancia()
        advanceTimeBy(limite + 1)
        runCurrent()

        viewModel.reautenticarConHuella()
        viewModel.reautenticarConHuella()
        runCurrent()

        assertTrue("El segundo toque no debe abrir otro diálogo", sensor.llamadas == 1)
        control.detener()
    }

    @Test
    fun `salir a la pantalla de ingreso retira la hoja`() = runTest(dispatcher) {
        val control = ControlDeInactividad(this)
        val viewModel = SesionViewModel(control, sensor)
        viewModel.iniciarVigilancia()
        advanceTimeBy(limite + 1)
        runCurrent()
        assertTrue(viewModel.expirada.value)

        viewModel.salirAIngreso()

        assertFalse(viewModel.expirada.value)
    }

    @Test
    fun `sin biometria disponible no se ofrece volver con huella`() = runTest(dispatcher) {
        sensor.disponibilidad = DisponibilidadBiometrica.NO_CONFIGURADA
        val control = ControlDeInactividad(this)
        val viewModel = SesionViewModel(control, sensor)

        assertFalse(viewModel.puedeReautenticarConHuella)
        control.detener()
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
