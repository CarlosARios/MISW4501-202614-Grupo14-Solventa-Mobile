package co.solventa.movil.core.auth.sesion

/**
 * Sesión del asegurado. El único rol humano de este canal.
 *
 * No guarda el token: ese vive en el almacenamiento cifrado del dispositivo y
 * lo adjunta el interceptor de `core/auth/interceptores`. Aquí queda lo que la
 * interfaz necesita mostrar.
 */
data class Sesion(
    val correo: String,
    val nombre: String,
)

sealed interface ResultadoIngreso {
    data class Exitoso(val sesion: Sesion) : ResultadoIngreso

    /**
     * No se dice si falló el correo o la contraseña: distinguirlos le permite
     * a un atacante descubrir qué correos están registrados.
     */
    data object CredencialesInvalidas : ResultadoIngreso

    data object SinConexion : ResultadoIngreso
}

interface SesionRepositorio {

    /** La sesión que quedó guardada en el dispositivo, si la hay. */
    suspend fun sesionGuardada(): Sesion?

    /** Si el usuario habilitó la biometría para Solventa en este dispositivo. */
    suspend fun biometriaHabilitada(): Boolean

    /**
     * Criterio 2 de HU-M1.1-02: desbloquea **localmente** la sesión guardada.
     * No hay petición al proveedor de identidad; los tokens ya estaban
     * emitidos y solo se liberan. Devuelve `null` si no había sesión.
     */
    suspend fun desbloquear(): Sesion?

    suspend fun ingresarConContrasena(correo: String, contrasena: String): ResultadoIngreso

    suspend fun habilitarBiometria(habilitada: Boolean)

    suspend fun cerrar()
}
