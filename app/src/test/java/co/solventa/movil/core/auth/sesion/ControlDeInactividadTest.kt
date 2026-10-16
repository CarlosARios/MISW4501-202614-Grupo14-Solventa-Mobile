package co.solventa.movil.core.auth.sesion

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Criterio 5 de HU-M1.1-02: cinco minutos de inactividad cierran la sesión.
 *
 * El tiempo es virtual, así que la prueba de los cinco minutos corre en
 * milisegundos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ControlDeInactividadTest {

    private val limite = ControlDeInactividad.LIMITE_DE_INACTIVIDAD_MS

    @Test
    fun `la sesion expira a los cinco minutos sin actividad`() = runTest {
        val control = ControlDeInactividad(this)
        control.iniciar()

        advanceTimeBy(limite - 1)
        runCurrent()
        assertFalse(control.expirada.value)

        advanceTimeBy(2)
        runCurrent()
        assertTrue(control.expirada.value)

        control.detener()
    }

    @Test
    fun `cada interaccion reinicia la cuenta`() = runTest {
        val control = ControlDeInactividad(this)
        control.iniciar()

        // Cuatro minutos y pico, y el usuario toca algo.
        advanceTimeBy(limite - 1_000)
        runCurrent()
        control.registrarActividad()

        // Pasa casi el límite otra vez: sigue viva porque el reloj se reinició.
        advanceTimeBy(limite - 1_000)
        runCurrent()
        assertFalse(control.expirada.value)

        advanceTimeBy(2_000)
        runCurrent()
        assertTrue(control.expirada.value)

        control.detener()
    }

    @Test
    fun `una vez expirada la actividad no la revive sola`() = runTest {
        val control = ControlDeInactividad(this)
        control.iniciar()
        advanceTimeBy(limite + 1)
        runCurrent()

        control.registrarActividad()
        runCurrent()

        assertTrue(
            "Solo volver a autenticarse debe reabrirla",
            control.expirada.value,
        )
        control.detener()
    }

    @Test
    fun `reanudar tras autenticarse devuelve la sesion y reinicia el reloj`() = runTest {
        val control = ControlDeInactividad(this)
        control.iniciar()
        advanceTimeBy(limite + 1)
        runCurrent()
        assertTrue(control.expirada.value)

        control.reanudar()
        runCurrent()
        assertFalse(control.expirada.value)

        advanceTimeBy(limite - 1)
        runCurrent()
        assertFalse(control.expirada.value)

        control.detener()
    }

    @Test
    fun `al detenerlo ya no expira`() = runTest {
        val control = ControlDeInactividad(this)
        control.iniciar()

        control.detener()
        advanceTimeBy(limite * 2)
        runCurrent()

        assertFalse(control.expirada.value)
    }
}
