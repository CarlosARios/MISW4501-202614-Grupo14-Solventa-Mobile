package co.solventa.movil.features.acceso.registro

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El repositorio simulado se prueba porque hoy es el que corre en la
 * aplicación, y porque su guion es lo que hace demostrable el recorrido
 * completo: si deja de fallar la primera vez, M1e y M1f se vuelven
 * inalcanzables sin que nadie se entere. La poca luz, en cambio, ya no la
 * finge: la mide la cámara en el dispositivo. Cuando lo reemplace el cliente real
 * de `core/api/`, estas pruebas se van con él.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistroKycRepositorioSimuladoTest {

    private val repositorio = RegistroKycRepositorioSimulado()

    private val frente = ImagenDocumento(CaraDocumento.FRENTE, "/tmp/frente.jpg")
    private val reverso = ImagenDocumento(CaraDocumento.REVERSO, "/tmp/reverso.jpg")

    @Test
    fun `el frente se acepta siempre y sin datos extraidos`() = runTest {
        val resultado = repositorio.analizarCaptura(frente)

        assertTrue(resultado is ResultadoCaptura.Aceptada)
        assertNull(
            "Los datos del documento salen del reverso",
            (resultado as ResultadoCaptura.Aceptada).datos,
        )
    }

    /**
     * La poca luz ya no se finge: la mide la cámara. Lo que el guion conserva
     * es la borrosidad, que es un juicio del proveedor de verificación y no
     * algo que el teléfono decida por su cuenta.
     */
    @Test
    fun `el primer intento del reverso falla por borrosidad para poder mostrar M1f`() = runTest {
        val resultado = repositorio.analizarCaptura(reverso)

        assertTrue(resultado is ResultadoCaptura.CalidadInsuficiente)
        assertEquals(
            ProblemaCalidad.IMAGEN_BORROSA,
            (resultado as ResultadoCaptura.CalidadInsuficiente).motivo,
        )
    }

    @Test
    fun `el segundo intento del reverso trae los datos del documento`() = runTest {
        repositorio.analizarCaptura(reverso)

        val resultado = repositorio.analizarCaptura(reverso)

        val datos = (resultado as ResultadoCaptura.Aceptada).datos
        assertEquals("Carolina", datos?.nombres)
        assertEquals("Pérez Ruiz", datos?.apellidos)
        assertEquals("CC", datos?.tipoDocumento)
        assertEquals("1.020.345.678", datos?.numeroDocumento)
    }

    @Test
    fun `la primera verificacion no se supera para poder mostrar M1e`() = runTest {
        val resultado = repositorio.verificarIdentidad(DATOS)

        assertTrue(resultado is ResultadoVerificacion.NoSuperada)
        assertEquals(
            "KYC-2026-77A2 · 16/09/2026 15:20",
            (resultado as ResultadoVerificacion.NoSuperada).referencia,
        )
    }

    @Test
    fun `la segunda verificacion es exitosa`() = runTest {
        repositorio.verificarIdentidad(DATOS)

        assertEquals(ResultadoVerificacion.Exitosa, repositorio.verificarIdentidad(DATOS))
    }

    private companion object {
        val DATOS = DatosDocumento(
            nombres = "Carolina",
            apellidos = "Pérez Ruiz",
            tipoDocumento = "CC",
            numeroDocumento = "1.020.345.678",
            fechaNacimiento = "14/05/1991",
        )
    }
}
