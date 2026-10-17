package co.solventa.movil.shared.ui.camara

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * La cámara trasera del dispositivo, vista desde una pantalla de captura.
 *
 * Envuelve CameraX para que las pantallas no tengan que conocer sus casos de
 * uso, su ciclo de vida ni sus ejecutores. Expone lo único que a la captura
 * del documento le importa: una vista previa, una foto y si hay luz.
 *
 * No es un singleton de Hilt: vive mientras la pantalla que lo creó, y se
 * suelta con ella. Dos pantallas de captura no comparten cámara.
 */
class ControladorDeCamara internal constructor(
    private val contexto: Context,
) {
    private var proveedor: ProcessCameraProvider? = null
    private var captura: ImageCapture? = null

    private val _luzInsuficiente = MutableStateFlow(false)

    /**
     * Si lo que ve la cámara está demasiado oscuro para que el documento se
     * lea. Arranca en `false`: mientras no haya una medida no se afirma que
     * falte luz, porque un aviso prematuro es peor que ninguno.
     */
    val luzInsuficiente: StateFlow<Boolean> = _luzInsuficiente.asStateFlow()

    /** Si el dispositivo tiene una cámara trasera que se pueda usar. */
    internal suspend fun tieneCamaraTrasera(): Boolean =
        runCatching {
            ProcessCameraProvider.awaitInstance(contexto)
                .also { proveedor = it }
                .hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        }.getOrDefault(false)

    /**
     * Enciende la cámara y la ata al ciclo de vida de la pantalla. Devuelve
     * `false` si el dispositivo no tiene cámara trasera utilizable.
     */
    internal suspend fun iniciar(dueño: LifecycleOwner, vista: PreviewView): Boolean {
        val proveedor = ProcessCameraProvider.awaitInstance(contexto).also { this.proveedor = it }
        if (!proveedor.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) return false

        val previa = Preview.Builder().build().apply { surfaceProvider = vista.surfaceProvider }
        val captura = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        val analisis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(ContextCompat.getMainExecutor(contexto), MedidorDeLuz { medida ->
                    _luzInsuficiente.value = MedidorDeLuz.esOscuro(medida)
                })
            }

        proveedor.unbindAll()
        proveedor.bindToLifecycle(dueño, CameraSelector.DEFAULT_BACK_CAMERA, previa, captura, analisis)
        this.captura = captura
        return true
    }

    internal fun detener() {
        proveedor?.unbindAll()
        proveedor = null
        captura = null
    }

    /**
     * Toma la foto y devuelve la ruta del archivo.
     *
     * El archivo va a la caché: es temporal, no entra en las copias de
     * seguridad y el sistema puede borrarlo. El documento de identidad no
     * tiene por qué sobrevivir al envío, que es lo que la pantalla le promete
     * al usuario.
     */
    suspend fun capturar(cara: String): String {
        val captura = checkNotNull(this.captura) { "La cámara no está lista" }
        val archivo = File(contexto.cacheDir, "documento-$cara-${System.currentTimeMillis()}.jpg")
        val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

        return suspendCancellableCoroutine { continuacion ->
            captura.takePicture(
                opciones,
                ContextCompat.getMainExecutor(contexto),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                        continuacion.resume(archivo.absolutePath)
                    }

                    override fun onError(excepcion: ImageCaptureException) {
                        continuacion.resumeWithException(excepcion)
                    }
                },
            )
        }
    }

}
