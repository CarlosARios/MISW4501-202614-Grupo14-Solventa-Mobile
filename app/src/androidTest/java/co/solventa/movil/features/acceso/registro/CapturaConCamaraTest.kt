package co.solventa.movil.features.acceso.registro

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import co.solventa.movil.R
import co.solventa.movil.shared.ui.camara.EstadoDeLaCamara
import co.solventa.movil.shared.ui.camara.camaraSimulada
import co.solventa.movil.ui.theme.SolventaTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * La captura con la cámara de verdad (HU-M1.1-01, criterios 1 y 4).
 *
 * Necesita permiso de cámara y una cámara trasera, así que el emulador de las
 * pruebas nocturnas arranca con `-camera-back emulated`. En un dispositivo
 * sin cámara la pantalla cae al marcador, y eso lo cubre
 * [PantallasRegistroTest].
 */
class CapturaConCamaraTest {

    @get:Rule
    val composicion = createComposeRule()

    private val instrumentacion = InstrumentationRegistry.getInstrumentation()
    private val contexto = instrumentacion.targetContext

    private fun texto(id: Int) = contexto.getString(id)

    /**
     * Se concede el permiso aquí y no con `GrantPermissionRule` para no sumar
     * una dependencia de pruebas por una sola línea; `uiAutomation` ya viene
     * con la instrumentación.
     */
    @Before
    fun concederLaCamara() {
        instrumentacion.uiAutomation.grantRuntimePermission(
            contexto.packageName,
            Manifest.permission.CAMERA,
        )
    }

    private fun montar() {
        composicion.setContent {
            SolventaTheme {
                CapturaDocumentoPantalla(
                    cara = CaraDocumento.FRENTE,
                    estado = RegistroUiState(),
                    onCapturar = {},
                    onLuzInsuficiente = {},
                    onDescartarAviso = {},
                    onAtras = {},
                )
            }
        }
    }

    /**
     * Preguntarle a CameraX si hay cámara trasera es asíncrono y no pasa por
     * el reloj de Compose, así que `waitForIdle` devuelve antes de tiempo:
     * hay que esperar al nodo.
     */
    private fun esperarALaVistaDeLaCamara() {
        composicion.waitUntil(timeoutMillis = 10_000) {
            composicion
                .onAllNodesWithContentDescription(
                    texto(R.string.registro_camara_vista_descripcion),
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun con_permiso_el_marco_muestra_la_camara_y_se_puede_capturar() {
        montar()
        esperarALaVistaDeLaCamara()

        composicion
            .onNodeWithContentDescription(texto(R.string.registro_camara_vista_descripcion))
            .assertIsDisplayed()
        composicion.onNodeWithText(texto(R.string.registro_captura_accion)).assertIsEnabled()
    }

    @Test
    fun con_permiso_ya_no_se_pide_el_permiso() {
        montar()
        esperarALaVistaDeLaCamara()

        composicion
            .onNodeWithText(texto(R.string.registro_camara_permiso_accion))
            .assertDoesNotExist()
    }

    @Test
    fun mientras_se_analiza_la_captura_el_boton_no_acepta_otra_foto() {
        composicion.setContent {
            SolventaTheme {
                CapturaDocumentoPantalla(
                    cara = CaraDocumento.FRENTE,
                    estado = RegistroUiState(analizando = true),
                    onCapturar = {},
                    onLuzInsuficiente = {},
                    onDescartarAviso = {},
                    onAtras = {},
                )
            }
        }
        composicion.waitForIdle()

        composicion.onNodeWithText(texto(R.string.registro_captura_accion)).assertIsNotEnabled()
    }

    /**
     * Criterio 4, de la medida a la pantalla.
     *
     * Con una cámara de mentira que reporta poca luz se ejerce todo el tramo
     * que el dedo sobre el lente probaría a mano: el aviso aparece y el botón
     * de capturar avisa en lugar de tomar la foto. Lo que el sensor mide lo
     * cubren las unitarias de `MedidorDeLuzTest`.
     */
    @Test
    fun con_poca_luz_se_avisa_y_no_se_toma_la_foto() {
        var avisoDePocaLuz = 0
        var fotosTomadas = 0

        composicion.setContent {
            SolventaTheme {
                CapturaDocumentoPantalla(
                    cara = CaraDocumento.FRENTE,
                    estado = RegistroUiState(),
                    onCapturar = { fotosTomadas++ },
                    onLuzInsuficiente = { avisoDePocaLuz++ },
                    onDescartarAviso = {},
                    onAtras = {},
                    camara = camaraSimulada(
                        estado = EstadoDeLaCamara.LISTA,
                        luzInsuficiente = true,
                    ),
                )
            }
        }
        composicion.waitForIdle()

        // `AvisoSolventa` fusiona su semántica con `clearAndSetSemantics`, así
        // que el texto de dentro no está en el árbol: se consulta por la
        // descripción. Y la pantalla se desplaza, con el aviso debajo del
        // marco, así que hay que bajar hasta él como haría el usuario.
        composicion
            .onNodeWithContentDescription(texto(R.string.registro_camara_poca_luz))
            .performScrollTo()
            .assertIsDisplayed()

        composicion.onNodeWithText(texto(R.string.registro_captura_accion)).performClick()
        composicion.waitForIdle()

        assertEquals("Debe avisar de la poca luz", 1, avisoDePocaLuz)
        assertEquals("No debe tomar la foto", 0, fotosTomadas)
    }

    @Test
    fun con_luz_suficiente_no_se_muestra_el_aviso() {
        montar()
        esperarALaVistaDeLaCamara()

        composicion
            .onNodeWithContentDescription(texto(R.string.registro_camara_poca_luz))
            .assertDoesNotExist()
    }
}
