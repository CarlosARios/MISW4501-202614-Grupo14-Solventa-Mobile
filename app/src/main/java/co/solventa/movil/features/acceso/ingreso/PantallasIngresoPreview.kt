package co.solventa.movil.features.acceso.ingreso

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import co.solventa.movil.ui.theme.SolventaTheme

/**
 * Vistas previas de las pantallas de ingreso, al tamaño del lienzo del
 * prototipo (390 × 844). Quedan fuera de la medición de cobertura.
 */
private const val ANCHO = 390
private const val ALTO = 844

private val SESION = IngresoUiState(preparando = false, nombre = "Carolina")

@Preview(name = "M2a · Ingreso con biometría", widthDp = ANCHO, heightDp = ALTO)
@Composable
private fun VistaPreviaBiometria() {
    SolventaTheme {
        IngresoBiometricoPantalla(
            estado = SESION,
            onReintentar = {},
            onUsarContrasena = {},
        )
    }
}

@Preview(name = "M2c · Primer intento fallido", widthDp = ANCHO, heightDp = ALTO)
@Composable
private fun VistaPreviaPrimerFallo() {
    SolventaTheme {
        IngresoBiometricoPantalla(
            estado = SESION.copy(intentosFallidos = 1),
            onReintentar = {},
            onUsarContrasena = {},
        )
    }
}

@Preview(name = "M2c · Intento 3 de 3 · pausada", widthDp = ANCHO, heightDp = ALTO)
@Composable
private fun VistaPreviaBiometriaPausada() {
    SolventaTheme {
        IngresoBiometricoPantalla(
            estado = SESION.copy(intentosFallidos = 3, biometriaPausada = true),
            onReintentar = {},
            onUsarContrasena = {},
        )
    }
}

@Preview(name = "M2b · Ingreso con contraseña", widthDp = ANCHO, heightDp = ALTO)
@Composable
private fun VistaPreviaContrasena() {
    SolventaTheme {
        IngresoContrasenaPantalla(
            estado = SESION.copy(
                metodo = MetodoDeIngreso.CONTRASENA,
                correo = "carolina.perez@correo.com",
                contrasena = "Solventa2026",
                habilitarBiometriaAlEntrar = true,
            ),
            onCorreo = {},
            onContrasena = {},
            onHabilitarBiometria = {},
            onEntrar = {},
            onOlvideContrasena = {},
            onCrearCuenta = {},
        )
    }
}

@Preview(name = "M2b · Credenciales inválidas", widthDp = ANCHO, heightDp = ALTO)
@Composable
private fun VistaPreviaContrasenaConError() {
    SolventaTheme {
        IngresoContrasenaPantalla(
            estado = SESION.copy(
                metodo = MetodoDeIngreso.CONTRASENA,
                correo = "carolina.perez@correo.com",
                contrasena = "Solventa2026",
                error = ErrorDeIngreso.CredencialesInvalidas,
            ),
            onCorreo = {},
            onContrasena = {},
            onHabilitarBiometria = {},
            onEntrar = {},
            onOlvideContrasena = {},
            onCrearCuenta = {},
        )
    }
}

@Preview(name = "M2d · Sesión expirada", widthDp = ANCHO, heightDp = 360)
@Composable
private fun VistaPreviaSesionExpirada() {
    SolventaTheme {
        Surface {
            Column {
                ContenidoSesionExpirada(onConHuella = {}, onConContrasena = {})
            }
        }
    }
}

@Preview(name = "M2a en inglés", widthDp = ANCHO, heightDp = ALTO, locale = "en")
@Composable
private fun VistaPreviaBiometriaIngles() {
    SolventaTheme {
        IngresoBiometricoPantalla(
            estado = SESION,
            onReintentar = {},
            onUsarContrasena = {},
        )
    }
}
