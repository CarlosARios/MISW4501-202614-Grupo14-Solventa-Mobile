package co.solventa.movil.features.acceso.ingreso

/** Por cuál de las dos vías está entrando el usuario. */
enum class MetodoDeIngreso { BIOMETRIA, CONTRASENA }

sealed interface ErrorDeIngreso {
    data object CredencialesInvalidas : ErrorDeIngreso
    data object SinConexion : ErrorDeIngreso

    /** El sistema bloqueó el sensor; hay que entrar con contraseña. */
    data object BiometriaNoDisponible : ErrorDeIngreso
}

data class IngresoUiState(
    val preparando: Boolean = true,
    val nombre: String = "",
    val metodo: MetodoDeIngreso = MetodoDeIngreso.BIOMETRIA,
    /**
     * Si este dispositivo puede ofrecer la huella. Se separa de [metodo]
     * porque a la pantalla de contraseña se llega por tres caminos y cada uno
     * merece una explicación distinta: no hay biometría, se agotaron los
     * intentos, o el usuario la prefirió.
     */
    val biometriaDisponible: Boolean = true,
    val solicitandoHuella: Boolean = false,
    val intentosFallidos: Int = 0,
    /**
     * Criterio 3: tras tres intentos fallidos la biometría queda pausada
     * **para esta sesión**, no bloqueada para siempre. En el próximo ingreso
     * vuelve a ofrecerse.
     */
    val biometriaPausada: Boolean = false,
    val correo: String = "",
    val contrasena: String = "",
    val habilitarBiometriaAlEntrar: Boolean = false,
    val verificando: Boolean = false,
    val error: ErrorDeIngreso? = null,
    val sesionAbierta: Boolean = false,
    /** La pantalla se abrió porque la sesión expiró por inactividad. */
    val porInactividad: Boolean = false,
) {
    val intentosRestantes: Int
        get() = (MAXIMO_DE_INTENTOS - intentosFallidos).coerceAtLeast(0)

    /** La longitud mínima la fija el diseño de M2b. */
    val contrasenaValida: Boolean
        get() = contrasena.length >= MINIMO_DE_CONTRASENA

    val puedeEntrar: Boolean
        get() = correo.isNotBlank() && contrasenaValida && !verificando

    companion object {
        const val MAXIMO_DE_INTENTOS = 3
        const val MINIMO_DE_CONTRASENA = 10
    }
}
