package co.solventa.movil.features.acceso.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Registro con verificación de identidad (HU-M1.1-01).
 *
 * Vive en el grafo de navegación del registro, no en una pantalla: el estado
 * tiene que sobrevivir el paso de M1a a M1h.
 */
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repositorio: RegistroKycRepositorio,
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    fun capturar(imagen: ImagenDocumento) {
        if (_estado.value.analizando) return
        viewModelScope.launch {
            _estado.update { it.copy(analizando = true, problemaCalidad = null) }
            when (val resultado = repositorio.analizarCaptura(imagen)) {
                is ResultadoCaptura.CalidadInsuficiente -> _estado.update {
                    it.copy(analizando = false, problemaCalidad = resultado.motivo)
                }

                is ResultadoCaptura.Aceptada -> _estado.update {
                    when (imagen.cara) {
                        CaraDocumento.FRENTE -> it.copy(analizando = false, frente = imagen)
                        CaraDocumento.REVERSO -> it.copy(
                            analizando = false,
                            reverso = imagen,
                            datos = resultado.datos ?: it.datos,
                        )
                    }
                }
            }
        }
    }

    /**
     * Criterio 4, medido en el dispositivo: la cámara reporta poca luz y la
     * foto no llega a tomarse.
     *
     * Antes este aviso lo fabricaba el repositorio simulado, que declaraba
     * oscura la primera captura aunque la habitación estuviera iluminada. Con
     * la cámara real la medida viene del sensor, así que el aviso aparece
     * cuando de verdad falta luz y desaparece cuando el usuario la corrige.
     */
    fun reportarLuzInsuficiente() {
        if (_estado.value.analizando) return
        _estado.update {
            it.copy(problemaCalidad = ProblemaCalidad.ILUMINACION_INSUFICIENTE)
        }
    }

    /** Cierra el aviso de calidad para volver a intentar la captura. */
    fun descartarProblemaCalidad() {
        _estado.update { it.copy(problemaCalidad = null) }
    }

    /** El usuario corrige en M1c un dato que el documento trajo mal. */
    fun actualizarDatos(datos: DatosDocumento) {
        _estado.update { it.copy(datos = datos) }
    }

    fun aceptarTerminos(aceptados: Boolean) {
        _estado.update { it.copy(terminosAceptados = aceptados) }
    }

    fun verificarIdentidad() {
        val datos = _estado.value.datos ?: return
        if (!_estado.value.puedeVerificar) return

        viewModelScope.launch {
            _estado.update { it.copy(verificacion = EstadoVerificacion.EnProceso) }

            val reloj = launch {
                delay(LIMITE_DE_ESPERA_MS)
                _estado.update {
                    if (it.verificacion == EstadoVerificacion.EnProceso) {
                        it.copy(verificacion = EstadoVerificacion.Demorada)
                    } else {
                        it
                    }
                }
            }

            val resultado = repositorio.verificarIdentidad(datos)
            reloj.cancel()

            _estado.update {
                // Si ya se le avisó al usuario que el resultado llega por
                // notificación, no se le cambia la pantalla debajo de los pies.
                if (it.verificacion == EstadoVerificacion.Demorada) {
                    it
                } else {
                    when (resultado) {
                        is ResultadoVerificacion.Exitosa ->
                            it.copy(verificacion = EstadoVerificacion.Exitosa)

                        is ResultadoVerificacion.NoSuperada ->
                            it.copy(verificacion = EstadoVerificacion.NoSuperada(resultado.referencia))
                    }
                }
            }
        }
    }

    /**
     * M1g · *Avísame por notificación*. El criterio 6 pide no mantener al
     * usuario esperando: puede salir antes de que se cumplan los 30 segundos.
     * El efecto es el mismo que el del temporizador.
     */
    fun avisarPorNotificacion() {
        _estado.update {
            if (it.verificacion == EstadoVerificacion.EnProceso) {
                it.copy(verificacion = EstadoVerificacion.Demorada)
            } else {
                it
            }
        }
    }

    /** M1e · *Intentar de nuevo* devuelve el flujo a la primera captura. */
    fun reiniciar() {
        _estado.value = RegistroUiState()
    }

    companion object {
        /** Criterio 6: pasados 30 segundos no se mantiene al usuario esperando. */
        const val LIMITE_DE_ESPERA_MS = 30_000L
    }
}
