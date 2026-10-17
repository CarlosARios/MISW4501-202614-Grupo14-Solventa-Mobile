package co.solventa.movil.core.auth.sesion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Criterio 5 de HU-M1.1-02: la sesión se cierra tras cinco minutos sin
 * actividad y hay que volver a autenticarse.
 *
 * Es un temporizador que se reinicia con cada interacción, no un reloj que se
 * consulta: así el aviso aparece exactamente al cumplirse el límite y no en la
 * siguiente revisión periódica. Vive en `core` porque la inactividad es de la
 * aplicación entera, no de una pantalla.
 */
@Singleton
class ControlDeInactividad @Inject constructor(
    private val alcance: CoroutineScope,
) {
    private val _expirada = MutableStateFlow(false)
    val expirada: StateFlow<Boolean> = _expirada.asStateFlow()

    private var temporizador: Job? = null

    /**
     * Solo se cuenta la inactividad mientras hay sesión abierta. Sin esta
     * bandera, un toque en bienvenida, registro o ingreso armaría el
     * temporizador y a los cinco minutos aparecería el aviso de sesión
     * expirada sin que nadie hubiera entrado.
     */
    private var vigilando = false

    /** Cada toque, cada navegación y cada respuesta del API lo llaman. */
    fun registrarActividad() {
        if (!vigilando || _expirada.value) return
        reiniciarTemporizador()
    }

    /** Arranca la vigilancia al abrir la sesión. */
    fun iniciar() {
        vigilando = true
        _expirada.value = false
        reiniciarTemporizador()
    }

    /** El usuario se volvió a autenticar: la sesión sigue donde estaba. */
    fun reanudar() {
        vigilando = true
        _expirada.value = false
        reiniciarTemporizador()
    }

    /**
     * Al cerrar sesión no hay nada que vigilar, y el aviso de expiración deja
     * de tener sentido: si el usuario va a autenticarse de nuevo desde cero,
     * la hoja no debe seguir encima.
     */
    fun detener() {
        vigilando = false
        temporizador?.cancel()
        temporizador = null
        _expirada.value = false
    }

    private fun reiniciarTemporizador() {
        temporizador?.cancel()
        temporizador = alcance.launch {
            delay(LIMITE_DE_INACTIVIDAD_MS)
            _expirada.value = true
        }
    }

    companion object {
        const val LIMITE_DE_INACTIVIDAD_MS = 5 * 60 * 1000L
    }
}
