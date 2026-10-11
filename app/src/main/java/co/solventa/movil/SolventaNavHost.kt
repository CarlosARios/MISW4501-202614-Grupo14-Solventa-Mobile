package co.solventa.movil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.solventa.movil.core.navegacion.Ruta
import co.solventa.movil.features.acceso.registro.grafoRegistro
import co.solventa.movil.shared.ui.BotonSolventa
import co.solventa.movil.shared.ui.TipoBoton
import co.solventa.movil.ui.theme.Espacio

/**
 * Navegación raíz de la aplicación.
 *
 * Vive en el paquete raíz y no en `core/navegacion` porque es la raíz de
 * composición: tiene que conocer todos los dominios, y `core` no puede
 * importar de `features`. En `core/navegacion` queda el contrato —[Ruta]—,
 * que sí es transversal.
 *
 * Hoy solo contiene el recorrido del registro (HU-M1.1-01) y dos marcadores:
 * M2a, que es de HU-M1.1-02, y M3, que corresponde a otra historia. Cada una
 * reemplaza su marcador cuando se construya.
 */
@Composable
fun SolventaNavHost(
    modifier: Modifier = Modifier,
    navegacion: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navegacion,
        startDestination = Ruta.Bienvenida,
        modifier = modifier,
    ) {
        composable<Ruta.Bienvenida> {
            BienvenidaPantalla(
                onCrearCuenta = { navegacion.navigate(Ruta.Registro) },
                onYaTengoCuenta = { navegacion.navigate(Ruta.Ingreso) },
            )
        }

        grafoRegistro(navegacion)

        composable<Ruta.Ingreso> { PantallaPendiente("M2a · Inicio de sesión · HU-M1.1-02") }
        composable<Ruta.Inicio> { PantallaPendiente("M3 · Inicio") }
    }
}

/**
 * M0 · Bienvenida. Versión mínima: es el punto de entrada que el recorrido
 * del registro necesita. Su construcción contra el frame es de HU-M1.1-02.
 */
@Composable
private fun BienvenidaPantalla(
    onCrearCuenta: () -> Unit,
    onYaTengoCuenta: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.x12, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(Espacio.x24),
    ) {
        Text(
            text = stringResource(R.string.bienvenida_titulo),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.bienvenida_lema),
            style = MaterialTheme.typography.bodyLarge,
        )
        BotonSolventa(
            texto = stringResource(R.string.bienvenida_crear_cuenta),
            onClick = onCrearCuenta,
            modifier = Modifier.padding(top = Espacio.x24),
        )
        BotonSolventa(
            texto = stringResource(R.string.bienvenida_ya_tengo_cuenta),
            onClick = onYaTengoCuenta,
            tipo = TipoBoton.SECUNDARIO,
        )
    }
}

@Composable
private fun PantallaPendiente(nombre: String) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(Espacio.x24),
    ) {
        Text(
            text = stringResource(R.string.pendiente_pantalla, nombre),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
