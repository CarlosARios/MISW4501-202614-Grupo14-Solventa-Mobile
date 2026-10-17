package co.solventa.movil.features.acceso.ingreso

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import co.solventa.movil.core.navegacion.Ruta

/**
 * M2a, M2b y M2c son un solo destino con tres estados, según la convención:
 * las variantes del prototipo no son rutas.
 */
fun NavGraphBuilder.destinoIngreso(navegacion: NavController) {
    composable<Ruta.Ingreso> {
        val viewModel: IngresoViewModel = hiltViewModel()
        val estado by viewModel.estado.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.preparar() }

        // Criterio 1: el diálogo del sistema aparece solo al abrir la
        // pantalla, sin que el usuario tenga que pulsar nada.
        LaunchedEffect(estado.preparando, estado.metodo) {
            if (!estado.preparando && estado.metodo == MetodoDeIngreso.BIOMETRIA) {
                viewModel.autenticarConHuella()
            }
        }

        LaunchedEffect(estado.sesionAbierta) {
            if (estado.sesionAbierta) {
                navegacion.navigate(Ruta.Inicio) {
                    popUpTo(Ruta.Bienvenida) { inclusive = true }
                }
            }
        }

        when (estado.metodo) {
            MetodoDeIngreso.BIOMETRIA -> IngresoBiometricoPantalla(
                estado = estado,
                onReintentar = viewModel::autenticarConHuella,
                onUsarContrasena = viewModel::cambiarAContrasena,
            )

            MetodoDeIngreso.CONTRASENA -> IngresoContrasenaPantalla(
                estado = estado,
                onCorreo = viewModel::actualizarCorreo,
                onContrasena = viewModel::actualizarContrasena,
                onHabilitarBiometria = viewModel::habilitarBiometriaAlEntrar,
                onEntrar = viewModel::entrarConContrasena,
                onOlvideContrasena = { },
                onCrearCuenta = { navegacion.navigate(Ruta.Registro) },
            )
        }
    }
}
