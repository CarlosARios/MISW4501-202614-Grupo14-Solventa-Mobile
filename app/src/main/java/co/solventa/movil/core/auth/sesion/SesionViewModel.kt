package co.solventa.movil.core.auth.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.solventa.movil.core.auth.biometria.AutenticadorBiometrico
import co.solventa.movil.core.auth.biometria.DisponibilidadBiometrica
import co.solventa.movil.core.auth.biometria.ResultadoBiometrico
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de sesión de toda la aplicación, no de una pantalla.
 *
 * Vive en el `NavHost` porque la expiración por inactividad (criterio 5 de
 * HU-M1.1-02) puede ocurrir en cualquier pantalla, y la hoja de M2d aparece
 * encima de donde esté el usuario: el mensaje promete devolverlo ahí mismo.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val control: ControlDeInactividad,
    private val autenticador: AutenticadorBiometrico,
) : ViewModel() {

    val expirada: StateFlow<Boolean> = control.expirada

    private val _reautenticando = MutableStateFlow(false)
    val reautenticando: StateFlow<Boolean> = _reautenticando.asStateFlow()

    /** Hay con qué volver sin salir a la pantalla de contraseña. */
    val puedeReautenticarConHuella: Boolean
        get() = autenticador.disponibilidad() == DisponibilidadBiometrica.DISPONIBLE

    /** La sesión acaba de abrirse: empieza a contar la inactividad. */
    fun iniciarVigilancia() = control.iniciar()

    /** Cualquier toque en la pantalla reinicia la cuenta. */
    fun registrarActividad() = control.registrarActividad()

    /**
     * M2d · *Autenticarme con huella*. Si el usuario se verifica, la sesión
     * sigue donde estaba; no hay que volver a entrar.
     */
    fun reautenticarConHuella() {
        // La bandera se marca **antes** de lanzar la corrutina: si se marcara
        // dentro, dos toques seguidos pasarían los dos y se abrirían dos
        // diálogos.
        if (!_reautenticando.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            val resultado = autenticador.autenticar()
            _reautenticando.value = false
            if (resultado == ResultadoBiometrico.Exito) {
                control.reanudar()
            }
        }
    }

    /**
     * M2d · *Usar mi contraseña*. Se deja de vigilar y la hoja desaparece,
     * porque el usuario va a autenticarse desde la pantalla de ingreso.
     */
    fun salirAIngreso() = control.detener()
}
