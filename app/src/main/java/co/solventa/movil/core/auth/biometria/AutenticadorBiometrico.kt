package co.solventa.movil.core.auth.biometria

/** Qué tan disponible está la biometría en este dispositivo. */
enum class DisponibilidadBiometrica {
    /** Hay hardware y el usuario tiene huella o rostro registrado. */
    DISPONIBLE,

    /** Hay hardware pero el usuario no ha registrado ninguna. */
    NO_CONFIGURADA,

    /** El dispositivo no tiene sensor, o está deshabilitado por el sistema. */
    SIN_HARDWARE,
}

sealed interface ResultadoBiometrico {
    /** La identidad se verificó. */
    data object Exito : ResultadoBiometrico

    /** La huella o el rostro no se reconocieron; el usuario puede reintentar. */
    data object NoReconocido : ResultadoBiometrico

    /** El usuario cerró el diálogo del sistema. */
    data object Cancelado : ResultadoBiometrico

    /**
     * El sistema operativo bloqueó el sensor —por demasiados intentos— o falló
     * por una causa que la aplicación no puede resolver.
     */
    data object NoDisponible : ResultadoBiometrico
}

/**
 * Frontera con el sensor del dispositivo.
 *
 * Existe como interfaz porque `BiometricPrompt` necesita una `FragmentActivity`
 * y no se puede ejercitar en la JVM: las pruebas del `ViewModel` usan un doble,
 * y la implementación real se verifica en un teléfono.
 */
interface AutenticadorBiometrico {

    fun disponibilidad(): DisponibilidadBiometrica

    /**
     * Muestra el diálogo del sistema y suspende hasta que el usuario responde.
     * La validación la hace el dispositivo: la aplicación nunca ve la huella.
     */
    suspend fun autenticar(): ResultadoBiometrico
}
