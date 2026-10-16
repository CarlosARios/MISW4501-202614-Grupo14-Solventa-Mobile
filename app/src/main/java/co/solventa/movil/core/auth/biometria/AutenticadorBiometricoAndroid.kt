package co.solventa.movil.core.auth.biometria

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import co.solventa.movil.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * El sensor de verdad, sobre `androidx.biometric`.
 *
 * La validación la hace el sistema operativo: la aplicación **nunca ve la
 * huella ni el rostro**, solo recibe un sí o un no. Eso es lo que dice la
 * pantalla del diálogo y es literalmente cierto.
 */
@Singleton
class AutenticadorBiometricoAndroid @Inject constructor(
    @ApplicationContext private val contexto: Context,
    private val actividades: ProveedorDeActividad,
) : AutenticadorBiometrico {

    override fun disponibilidad(): DisponibilidadBiometrica =
        when (BiometricManager.from(contexto).canAuthenticate(AUTENTICADORES)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                DisponibilidadBiometrica.DISPONIBLE

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                DisponibilidadBiometrica.NO_CONFIGURADA

            else -> DisponibilidadBiometrica.SIN_HARDWARE
        }

    override suspend fun autenticar(): ResultadoBiometrico {
        val actividad = actividades.actual() ?: return ResultadoBiometrico.NoDisponible

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuacion ->
                // El diálogo puede avisar dos veces —primero el fallo y luego
                // la cancelación que nosotros mismos provocamos—, así que la
                // respuesta se entrega una sola vez.
                val respondido = AtomicBoolean(false)
                fun responder(resultado: ResultadoBiometrico) {
                    if (respondido.compareAndSet(false, true) && continuacion.isActive) {
                        continuacion.resume(resultado)
                    }
                }

                lateinit var dialogo: BiometricPrompt

                dialogo = BiometricPrompt(
                    actividad,
                    ContextCompat.getMainExecutor(contexto),
                    object : BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(
                            resultado: BiometricPrompt.AuthenticationResult,
                        ) {
                            responder(ResultadoBiometrico.Exito)
                        }

                        /**
                         * Se leyó una huella y no coincide. Android deja el
                         * diálogo abierto para reintentar ahí mismo, pero el
                         * prototipo lleva al usuario a M2c con su contador,
                         * así que lo cerramos y devolvemos el control.
                         */
                        override fun onAuthenticationFailed() {
                            responder(ResultadoBiometrico.NoReconocido)
                            dialogo.cancelAuthentication()
                        }

                        override fun onAuthenticationError(
                            codigo: Int,
                            mensaje: CharSequence,
                        ) {
                            responder(traducir(codigo))
                        }
                    },
                )

                dialogo.authenticate(descripcionDelDialogo())
                continuacion.invokeOnCancellation { dialogo.cancelAuthentication() }
            }
        }
    }

    private fun descripcionDelDialogo(): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(contexto.getString(R.string.ingreso_prompt_titulo))
            .setDescription(contexto.getString(R.string.ingreso_prompt_detalle))
            .setNegativeButtonText(contexto.getString(R.string.ingreso_prompt_cancelar))
            .setAllowedAuthenticators(AUTENTICADORES)
            .setConfirmationRequired(false)
            .build()

    private fun traducir(codigo: Int): ResultadoBiometrico = when (codigo) {
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        BiometricPrompt.ERROR_USER_CANCELED,
        BiometricPrompt.ERROR_CANCELED,
        -> ResultadoBiometrico.Cancelado

        // El sistema bloqueó el sensor por sus propios reintentos, o no hay
        // con qué autenticar. En los dos casos la salida es la contraseña.
        else -> ResultadoBiometrico.NoDisponible
    }

    private companion object {
        const val AUTENTICADORES = BIOMETRIC_STRONG or BIOMETRIC_WEAK
    }
}
