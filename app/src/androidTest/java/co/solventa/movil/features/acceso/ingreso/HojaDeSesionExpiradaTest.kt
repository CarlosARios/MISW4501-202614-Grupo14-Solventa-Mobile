package co.solventa.movil.features.acceso.ingreso

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.platform.app.InstrumentationRegistry
import co.solventa.movil.R
import co.solventa.movil.ui.theme.SolventaTheme
import org.junit.Rule
import org.junit.Test

/**
 * M2d no se puede ignorar: la sesión expiró de verdad y hay que resolverla
 * con la huella o con la contraseña.
 *
 * Es una regresión encontrada en el Moto G20. Con `onDismissRequest` vacío,
 * tocar el velo ocultaba la hoja y Material3 dejaba su ventana encima
 * tragándose todos los toques: la aplicación quedaba inservible sin que se
 * viera nada raro en pantalla.
 */
class HojaDeSesionExpiradaTest {

    @get:Rule
    val composicion = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    private fun texto(id: Int) = contexto.getString(id)

    private fun montarHoja() {
        composicion.setContent {
            SolventaTheme {
                HojaDeSesionExpirada(onConHuella = {}, onConContrasena = {})
            }
        }
    }

    @Test
    fun la_hoja_ofrece_las_dos_salidas() {
        montarHoja()

        composicion.onNodeWithText(texto(R.string.sesion_expirada_titulo)).assertIsDisplayed()
        composicion.onNodeWithText(texto(R.string.sesion_expirada_con_huella)).assertIsDisplayed()
        composicion
            .onNodeWithText(texto(R.string.sesion_expirada_con_contrasena))
            .assertIsDisplayed()
    }

    @Test
    fun tocar_por_fuera_no_cierra_la_hoja() {
        montarHoja()

        // El velo no tiene texto ni identificador propio: se localiza por la
        // descripción que Compose le pone (`close_sheet`), y el idioma del
        // dispositivo decide cuál de las dos es. El emulador nocturno va
        // fijado en es-CO, pero los teléfonos de prueba del equipo no tienen
        // por qué estarlo, así que se aceptan las dos.
        composicion
            .onAllNodes(
                hasContentDescription("Close sheet").or(hasContentDescription("Cerrar hoja")),
            )
            .onFirst()
            .performClick()
        composicion.waitForIdle()

        composicion.onNodeWithText(texto(R.string.sesion_expirada_titulo)).assertIsDisplayed()
    }

    @Test
    fun arrastrarla_hacia_abajo_tampoco_la_cierra() {
        montarHoja()

        composicion
            .onNodeWithText(texto(R.string.sesion_expirada_titulo))
            .performTouchInput { swipeDown() }
        composicion.waitForIdle()

        composicion.onNodeWithText(texto(R.string.sesion_expirada_titulo)).assertIsDisplayed()
    }
}
