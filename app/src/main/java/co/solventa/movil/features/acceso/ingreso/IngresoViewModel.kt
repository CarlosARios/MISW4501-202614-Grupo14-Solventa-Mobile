package co.solventa.movil.features.acceso.ingreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.solventa.movil.core.auth.biometria.AutenticadorBiometrico
import co.solventa.movil.core.auth.biometria.DisponibilidadBiometrica
import co.solventa.movil.core.auth.biometria.ResultadoBiometrico
import co.solventa.movil.core.auth.sesion.ResultadoIngreso
import co.solventa.movil.core.auth.sesion.SesionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ingreso con la biometría del dispositivo (HU-M1.1-02).
 *
 * Las dos vías —huella y contraseña— viven en el mismo `ViewModel` porque la
 * historia las trata como un solo flujo: la segunda es la salida de la
 * primera, y el estado de los intentos tiene que sobrevivir el cambio.
 */
@HiltViewModel
class IngresoViewModel @Inject constructor(
    private val repositorio: SesionRepositorio,
    private val autenticador: AutenticadorBiometrico,
) : ViewModel() {

    private val _estado = MutableStateFlow(IngresoUiState())
    val estado: StateFlow<IngresoUiState> = _estado.asStateFlow()

    /**
     * Decide con qué método se abre la pantalla.
     *
     * Criterio 1: con biometría disponible y sesión guardada, se ofrece la
     * huella. Criterio 4: sin biometría configurada, o sin haberla habilitado
     * para Solventa, se va directo al formulario de contraseña.
     */
    fun preparar(porInactividad: Boolean = false) {
        viewModelScope.launch {
            val sesion = repositorio.sesionGuardada()
            val habilitada = repositorio.biometriaHabilitada()
            val disponible = autenticador.disponibilidad() == DisponibilidadBiometrica.DISPONIBLE

            val puedeUsarHuella = sesion != null && habilitada && disponible

            _estado.update {
                it.copy(
                    preparando = false,
                    nombre = sesion?.nombre.orEmpty(),
                    correo = sesion?.correo.orEmpty(),
                    biometriaDisponible = disponible && habilitada,
                    metodo = if (puedeUsarHuella) {
                        MetodoDeIngreso.BIOMETRIA
                    } else {
                        MetodoDeIngreso.CONTRASENA
                    },
                    porInactividad = porInactividad,
                )
            }
        }
    }

    /**
     * Criterio 1: el diálogo del sistema se muestra solo al abrir la pantalla,
     * sin que el usuario tenga que pulsar nada. También lo invoca el botón de
     * reintentar.
     */
    fun autenticarConHuella() {
        val actual = _estado.value
        if (actual.solicitandoHuella || actual.biometriaPausada) return
        if (actual.metodo != MetodoDeIngreso.BIOMETRIA) return

        viewModelScope.launch {
            _estado.update { it.copy(solicitandoHuella = true, error = null) }

            when (autenticador.autenticar()) {
                ResultadoBiometrico.Exito -> desbloquearSesion()

                ResultadoBiometrico.NoReconocido -> registrarIntentoFallido()

                // Cerrar el diálogo no cuenta como intento: el usuario no
                // falló, decidió no usarlo.
                ResultadoBiometrico.Cancelado ->
                    _estado.update { it.copy(solicitandoHuella = false) }

                ResultadoBiometrico.NoDisponible -> _estado.update {
                    it.copy(
                        solicitandoHuella = false,
                        biometriaPausada = true,
                        metodo = MetodoDeIngreso.CONTRASENA,
                        error = ErrorDeIngreso.BiometriaNoDisponible,
                    )
                }
            }
        }
    }

    private suspend fun desbloquearSesion() {
        // Criterio 2: desbloqueo local, sin ir al proveedor de identidad.
        val sesion = repositorio.desbloquear()
        _estado.update {
            if (sesion == null) {
                it.copy(solicitandoHuella = false, metodo = MetodoDeIngreso.CONTRASENA)
            } else {
                it.copy(solicitandoHuella = false, sesionAbierta = true, nombre = sesion.nombre)
            }
        }
    }

    /**
     * Criterio 3: hasta tres intentos. Al tercero la biometría queda pausada
     * para esta sesión y se ofrece la contraseña.
     */
    private fun registrarIntentoFallido() {
        _estado.update {
            val fallidos = it.intentosFallidos + 1
            val agotados = fallidos >= IngresoUiState.MAXIMO_DE_INTENTOS
            it.copy(
                solicitandoHuella = false,
                intentosFallidos = fallidos,
                biometriaPausada = agotados,
                metodo = if (agotados) MetodoDeIngreso.CONTRASENA else it.metodo,
            )
        }
    }

    /** M2c · *Ingresar con contraseña*, sin esperar a agotar los intentos. */
    fun cambiarAContrasena() {
        _estado.update { it.copy(metodo = MetodoDeIngreso.CONTRASENA, error = null) }
    }

    fun actualizarCorreo(correo: String) {
        _estado.update { it.copy(correo = correo, error = null) }
    }

    fun actualizarContrasena(contrasena: String) {
        _estado.update { it.copy(contrasena = contrasena, error = null) }
    }

    fun habilitarBiometriaAlEntrar(habilitar: Boolean) {
        _estado.update { it.copy(habilitarBiometriaAlEntrar = habilitar) }
    }

    fun entrarConContrasena() {
        val actual = _estado.value
        if (!actual.puedeEntrar) return

        viewModelScope.launch {
            _estado.update { it.copy(verificando = true, error = null) }

            when (val resultado =
                repositorio.ingresarConContrasena(actual.correo, actual.contrasena)) {
                is ResultadoIngreso.Exitoso -> {
                    if (actual.habilitarBiometriaAlEntrar) {
                        repositorio.habilitarBiometria(true)
                    }
                    _estado.update {
                        it.copy(
                            verificando = false,
                            sesionAbierta = true,
                            nombre = resultado.sesion.nombre,
                            contrasena = "",
                        )
                    }
                }

                ResultadoIngreso.CredencialesInvalidas -> _estado.update {
                    it.copy(verificando = false, error = ErrorDeIngreso.CredencialesInvalidas)
                }

                ResultadoIngreso.SinConexion -> _estado.update {
                    it.copy(verificando = false, error = ErrorDeIngreso.SinConexion)
                }
            }
        }
    }
}
