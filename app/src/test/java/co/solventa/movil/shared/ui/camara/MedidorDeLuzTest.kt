package co.solventa.movil.shared.ui.camara

import androidx.camera.core.ImageProxy
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer

/**
 * El criterio 4 de HU-M1.1-01 descansa en esta medida, así que conviene que
 * la aritmética esté probada: si el promedio sale mal, o la aplicación no
 * avisa cuando está oscuro, o molesta con un aviso falso cada vez.
 */
class MedidorDeLuzTest {

    @Test
    fun `promedia la luminancia del plano Y`() {
        var medida = -1
        MedidorDeLuz { medida = it }.analyze(imagenCon(valor = 200, bytes = 4_096))

        assertEquals(200, medida)
    }

    @Test
    fun `el lente tapado da una medida baja`() {
        var medida = -1
        MedidorDeLuz { medida = it }.analyze(imagenCon(valor = 8, bytes = 4_096))

        assertTrue("Con el lente tapado debe considerarse oscuro", esOscuro(medida))
    }

    @Test
    fun `un documento bien iluminado no dispara el aviso`() {
        var medida = -1
        MedidorDeLuz { medida = it }.analyze(imagenCon(valor = 130, bytes = 4_096))

        assertFalse(esOscuro(medida))
    }

    /**
     * El byte de luminancia es con signo en Java y va de 0 a 255 en realidad.
     * Sin el `and 0xFF` todo lo que pase de 127 se leería negativo y las
     * escenas más claras saldrían como las más oscuras.
     */
    @Test
    fun `los valores por encima de 127 no se leen negativos`() {
        var medida = -1
        MedidorDeLuz { medida = it }.analyze(imagenCon(valor = 240, bytes = 4_096))

        assertEquals(240, medida)
    }

    @Test
    fun `una imagen sin planos no reporta nada`() {
        var llamadas = 0
        val imagen = mockk<ImageProxy>(relaxed = true)
        every { imagen.planes } returns emptyArray()

        MedidorDeLuz { llamadas++ }.analyze(imagen)

        assertEquals(0, llamadas)
    }

    private fun esOscuro(medida: Int) = MedidorDeLuz.esOscuro(medida)

    private fun imagenCon(valor: Int, bytes: Int): ImageProxy {
        val bufer = ByteBuffer.allocate(bytes).apply {
            repeat(bytes) { put(valor.toByte()) }
            rewind()
        }
        val plano = mockk<ImageProxy.PlaneProxy>(relaxed = true)
        every { plano.buffer } returns bufer

        val imagen = mockk<ImageProxy>(relaxed = true)
        every { imagen.planes } returns arrayOf(plano)
        return imagen
    }
}
