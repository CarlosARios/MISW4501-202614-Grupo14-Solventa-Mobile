package co.solventa.movil.shared.ui.camara

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Todo lo que una pantalla de captura necesita saber de la cámara: en qué
 * estado está, si hay luz y cómo pedir el permiso.
 */
class CamaraDeDocumento internal constructor(
    val estado: EstadoDeLaCamara,
    val luzInsuficiente: Boolean,
    val controlador: ControladorDeCamara?,
    val pedirPermiso: () -> Unit,
)

/**
 * Una cámara de mentira, con el estado que se le pida.
 *
 * La usan las vistas previas y las pruebas de interfaz. Sin ella, la pantalla
 * mostraría el marcador o la vista en vivo según el permiso que tenga
 * concedido el aparato donde corran, y la misma prueba pasaría o fallaría
 * según el teléfono. También es la única forma de ejercer el aviso de poca
 * luz sin tapar el lente con un dedo.
 */
fun camaraSimulada(
    estado: EstadoDeLaCamara = EstadoDeLaCamara.SIN_CAMARA,
    luzInsuficiente: Boolean = false,
): CamaraDeDocumento =
    CamaraDeDocumento(estado, luzInsuficiente, controlador = null) {}

/**
 * Prepara la cámara para la pantalla que la llama: pregunta por el permiso,
 * averigua si hay cámara trasera y la suelta al salir.
 *
 * Nunca lanza. Si no hay permiso o no hay cámara, devuelve el estado que lo
 * dice y la pantalla decide qué dibujar. Eso es lo que permite que la captura
 * del documento siga siendo navegable en un emulador sin cámara, que es como
 * pueden correr las pruebas nocturnas.
 */
@Composable
fun recordarCamaraDeDocumento(): CamaraDeDocumento {
    val contexto = LocalContext.current

    // En el panel de diseño de Android Studio no hay cámara ni permisos, y
    // pedirlos reventaría las vistas previas.
    if (LocalInspectionMode.current) {
        return CamaraDeDocumento(EstadoDeLaCamara.SIN_CAMARA, false, null) {}
    }

    var concedido by remember { mutableStateOf(contexto.tienePermisoDeCamara()) }
    var yaSePregunto by remember { mutableStateOf(false) }
    var hayCamara by remember { mutableStateOf<Boolean?>(null) }

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { respuesta ->
        concedido = respuesta
        yaSePregunto = true
    }

    val controlador = remember { ControladorDeCamara(contexto) }
    val luzInsuficiente by controlador.luzInsuficiente.collectAsStateWithLifecycle()

    LaunchedEffect(concedido) {
        if (concedido) hayCamara = controlador.tieneCamaraTrasera()
    }

    DisposableEffect(Unit) {
        onDispose { controlador.detener() }
    }

    val estado = when {
        !concedido && yaSePregunto -> EstadoDeLaCamara.PERMISO_NEGADO
        !concedido -> EstadoDeLaCamara.SIN_PERMISO
        hayCamara == null -> EstadoDeLaCamara.CONSULTANDO
        hayCamara == false -> EstadoDeLaCamara.SIN_CAMARA
        else -> EstadoDeLaCamara.LISTA
    }

    return CamaraDeDocumento(
        estado = estado,
        luzInsuficiente = luzInsuficiente,
        controlador = controlador.takeIf { estado == EstadoDeLaCamara.LISTA },
        pedirPermiso = { lanzador.launch(Manifest.permission.CAMERA) },
    )
}

/**
 * La vista previa en vivo. Se dibuja solo cuando el estado es
 * [EstadoDeLaCamara.LISTA]; en cualquier otro caso la pantalla pone su propio
 * marcador en su lugar.
 */
@Composable
fun VistaPreviaDeCamara(
    camara: CamaraDeDocumento,
    modifier: Modifier = Modifier,
) {
    val controlador = camara.controlador ?: return
    val dueño = LocalLifecycleOwner.current
    val contexto = LocalContext.current
    val vista = remember {
        PreviewView(contexto).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(controlador, vista, dueño) { controlador.iniciar(dueño, vista) }

    AndroidView(factory = { vista }, modifier = modifier)
}

internal fun Context.tienePermisoDeCamara(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
