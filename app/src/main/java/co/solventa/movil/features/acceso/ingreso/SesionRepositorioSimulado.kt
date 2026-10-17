package co.solventa.movil.features.acceso.ingreso

import co.solventa.movil.core.auth.sesion.ResultadoIngreso
import co.solventa.movil.core.auth.sesion.Sesion
import co.solventa.movil.core.auth.sesion.SesionRepositorio
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación temporal del repositorio de sesión.
 *
 * **Por qué existe:** el *user pool* de Cognito todavía no está creado en la
 * cuenta del proyecto, así que no hay contra qué autenticar. El criterio 2 de
 * la historia es un desbloqueo **local**, de modo que la mayor parte del flujo
 * no necesita red ni ahora ni después; lo único que este doble reemplaza es el
 * ingreso con contraseña de M2b.
 *
 * **Qué la reemplaza:** el cliente de Cognito en `core/auth/sesion` cuando el
 * equipo publique la región, el `userPoolId` y el `clientId`. La interfaz
 * [SesionRepositorio] no cambia. Esta clase y sus pruebas se borran juntas.
 *
 * Los datos son los de los frames M2a y M2b del prototipo.
 */
@Singleton
class SesionRepositorioSimulado @Inject constructor() : SesionRepositorio {

    private var sesion: Sesion? = SESION_DE_EJEMPLO
    private var biometria = true

    override suspend fun sesionGuardada(): Sesion? = sesion

    override suspend fun biometriaHabilitada(): Boolean = biometria

    override suspend fun desbloquear(): Sesion? {
        // Desbloqueo local: sin red, por eso es inmediato.
        return sesion
    }

    override suspend fun ingresarConContrasena(
        correo: String,
        contrasena: String,
    ): ResultadoIngreso {
        delay(DEMORA_INGRESO_MS)
        return if (correo.equals(CORREO, ignoreCase = true) && contrasena == CONTRASENA) {
            sesion = SESION_DE_EJEMPLO
            ResultadoIngreso.Exitoso(SESION_DE_EJEMPLO)
        } else {
            ResultadoIngreso.CredencialesInvalidas
        }
    }

    override suspend fun habilitarBiometria(habilitada: Boolean) {
        biometria = habilitada
    }

    override suspend fun cerrar() {
        sesion = null
    }

    private companion object {
        const val DEMORA_INGRESO_MS = 1_500L
        const val CORREO = "carolina.perez@correo.com"
        const val CONTRASENA = "Solventa2026"

        val SESION_DE_EJEMPLO = Sesion(correo = CORREO, nombre = "Carolina")
    }
}
