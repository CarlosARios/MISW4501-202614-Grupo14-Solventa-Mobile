package co.solventa.movil.core.auth.biometria

import androidx.fragment.app.FragmentActivity
import io.mockk.mockk
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * El proveedor no toca el framework: solo guarda y entrega una referencia, y
 * eso sí se puede verificar en la JVM con un doble del tipo.
 */
class ProveedorDeActividadTest {

    private val proveedor = ProveedorDeActividad()

    @Test
    fun `sin actividad registrada no entrega ninguna`() {
        assertNull(proveedor.actual())
    }

    @Test
    fun `entrega la actividad que se registro`() {
        val actividad = mockk<FragmentActivity>()

        proveedor.registrar(actividad)

        assertSame(actividad, proveedor.actual())
    }

    @Test
    fun `al olvidarla deja de entregarla`() {
        proveedor.registrar(mockk<FragmentActivity>())

        proveedor.olvidar()

        assertNull(proveedor.actual())
    }

    @Test
    fun `registrar una nueva reemplaza a la anterior`() {
        val primera = mockk<FragmentActivity>()
        val segunda = mockk<FragmentActivity>()

        proveedor.registrar(primera)
        proveedor.registrar(segunda)

        assertSame(segunda, proveedor.actual())
    }
}
