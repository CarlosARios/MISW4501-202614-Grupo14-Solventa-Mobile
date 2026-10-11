package co.solventa.movil.features.acceso.registro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import co.solventa.movil.R
import co.solventa.movil.shared.ui.BotonSolventa
import co.solventa.movil.shared.ui.FilaEstado
import co.solventa.movil.shared.ui.ListaConsejos
import co.solventa.movil.shared.ui.MedallonEstado
import co.solventa.movil.shared.ui.PantallaRegistro
import co.solventa.movil.shared.ui.SeparadorTarjeta
import co.solventa.movil.shared.ui.TarjetaSolventa
import co.solventa.movil.shared.ui.TipoBoton
import co.solventa.movil.ui.theme.Espacio
import co.solventa.movil.ui.theme.LocalColoresSolventa

/**
 * M1g · verificando identidad.
 *
 * Criterio 6, primera mitad: el usuario ve qué está pasando y no queda
 * bloqueado. El botón le permite salir y recibir el resultado por
 * notificación sin esperar a que se cumplan los 30 segundos.
 */
@Composable
fun VerificandoPantalla(
    onAvisarPorNotificacion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresSolventa.current

    PantallaRegistro(
        titulo = stringResource(R.string.registro_verificando_titulo),
        modifier = modifier,
        acciones = {
            BotonSolventa(
                texto = stringResource(R.string.registro_verificando_avisarme),
                onClick = onAvisarPorNotificacion,
                tipo = TipoBoton.SECUNDARIO,
                icono = painterResource(R.drawable.ic_campana),
            )
        },
    ) {
        PasoDelRegistro(
            avance = 1f,
            etiqueta = stringResource(R.string.registro_verificacion_paso),
            color = colores.informacion,
        )

        Encabezado(
            icono = R.drawable.ic_reloj,
            tinte = colores.informacionFuerte,
            fondo = colores.informacionSuave,
            titulo = stringResource(R.string.registro_verificando_encabezado),
            detalle = stringResource(R.string.registro_verificando_detalle),
        )

        TarjetaSolventa {
            FilaEstado(
                icono = painterResource(R.drawable.ic_circulo_check),
                tinte = colores.exitoFuerte,
                texto = stringResource(R.string.registro_verificando_documento),
            )
            SeparadorTarjeta()
            FilaEstado(
                icono = painterResource(R.drawable.ic_reloj),
                tinte = colores.informacionFuerte,
                texto = stringResource(R.string.registro_verificando_prueba_vida),
            )
            SeparadorTarjeta()
            FilaEstado(
                icono = painterResource(R.drawable.ic_reloj),
                tinte = colores.textoSecundario,
                texto = stringResource(R.string.registro_verificando_listas),
            )
        }
    }
}

/** M1d · verificación exitosa. Criterio 2: la cuenta queda activa. */
@Composable
fun VerificacionExitosaPantalla(
    onIrAlInicio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresSolventa.current
    var biometria by remember { mutableStateOf(true) }

    PantallaRegistro(
        titulo = stringResource(R.string.registro_exito_titulo),
        modifier = modifier,
        acciones = {
            BotonSolventa(
                texto = stringResource(R.string.registro_exito_accion),
                onClick = onIrAlInicio,
            )
        },
    ) {
        PasoDelRegistro(
            avance = 1f,
            etiqueta = stringResource(R.string.registro_verificacion_paso),
            color = colores.exito,
        )

        Encabezado(
            icono = R.drawable.ic_circulo_check,
            tinte = colores.exitoFuerte,
            fondo = colores.exitoSuave,
            titulo = stringResource(R.string.registro_exito_encabezado),
            detalle = stringResource(R.string.registro_exito_detalle),
        )

        TarjetaSolventa {
            FilaEstado(
                icono = painterResource(R.drawable.ic_circulo_check),
                tinte = colores.exitoFuerte,
                texto = stringResource(R.string.registro_exito_documento),
            )
            SeparadorTarjeta()
            FilaEstado(
                icono = painterResource(R.drawable.ic_circulo_check),
                tinte = colores.exitoFuerte,
                texto = stringResource(R.string.registro_exito_prueba_vida),
            )
            SeparadorTarjeta()
            FilaEstado(
                icono = painterResource(R.drawable.ic_circulo_check),
                tinte = colores.exitoFuerte,
                texto = stringResource(R.string.registro_exito_listas),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espacio.x12),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Switch(checked = biometria, onCheckedChange = { biometria = it })
            Text(
                text = stringResource(R.string.registro_exito_biometria),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * M1e · verificación no superada.
 *
 * Criterio 3: no se dice por qué falló. Los consejos son generales y la
 * referencia sirve para que soporte rastree el intento.
 */
@Composable
fun VerificacionNoSuperadaPantalla(
    referencia: String,
    onIntentarDeNuevo: () -> Unit,
    onContactarSoporte: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresSolventa.current

    PantallaRegistro(
        titulo = stringResource(R.string.registro_fallo_titulo),
        modifier = modifier,
        acciones = {
            BotonSolventa(
                texto = stringResource(R.string.registro_fallo_accion),
                onClick = onIntentarDeNuevo,
            )
            BotonSolventa(
                texto = stringResource(R.string.comun_contactar_soporte),
                onClick = onContactarSoporte,
                tipo = TipoBoton.TERCIARIO,
            )
        },
    ) {
        Encabezado(
            icono = R.drawable.ic_circulo_x,
            tinte = MaterialTheme.colorScheme.error,
            fondo = MaterialTheme.colorScheme.errorContainer,
            titulo = stringResource(R.string.registro_fallo_encabezado),
            detalle = stringResource(R.string.registro_fallo_detalle),
        )

        TarjetaSolventa {
            Box(modifier = Modifier.fillMaxWidth()) {
                ListaConsejos(
                    consejos = listOf(
                        stringResource(R.string.registro_fallo_consejo_luz),
                        stringResource(R.string.registro_fallo_consejo_documento),
                        stringResource(R.string.registro_fallo_consejo_rostro),
                    ),
                    icono = painterResource(R.drawable.ic_check),
                    tinte = colores.textoSecundario,
                    modifier = Modifier.padding(vertical = Espacio.x16),
                )
            }
        }

        Text(
            text = stringResource(R.string.registro_fallo_referencia, referencia),
            style = MaterialTheme.typography.bodySmall,
            color = colores.textoSecundario,
        )
    }
}

/**
 * M1h · la verificación tarda.
 *
 * Criterio 6, segunda mitad, y criterio 5: se avisa que el resultado llega
 * por notificación y se dice explícitamente que la cuenta no está activa.
 * *Entendido* sale a la bienvenida, no al inicio, justamente por eso.
 */
@Composable
fun VerificacionDemoradaPantalla(
    onEntendido: () -> Unit,
    onContactarSoporte: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresSolventa.current

    PantallaRegistro(
        titulo = stringResource(R.string.registro_demora_titulo),
        modifier = modifier,
        acciones = {
            BotonSolventa(
                texto = stringResource(R.string.comun_entendido),
                onClick = onEntendido,
                icono = painterResource(R.drawable.ic_circulo_check),
            )
            BotonSolventa(
                texto = stringResource(R.string.comun_contactar_soporte),
                onClick = onContactarSoporte,
                tipo = TipoBoton.TERCIARIO,
            )
        },
    ) {
        PasoDelRegistro(
            avance = 1f,
            etiqueta = stringResource(R.string.registro_verificacion_paso),
            color = colores.advertencia,
        )

        Encabezado(
            icono = R.drawable.ic_campana,
            tinte = colores.advertenciaFuerte,
            fondo = colores.advertenciaSuave,
            titulo = stringResource(R.string.registro_demora_encabezado),
            detalle = stringResource(R.string.registro_demora_detalle),
        )

        TarjetaSolventa {
            FilaEstado(
                icono = painterResource(R.drawable.ic_circulo_check),
                tinte = colores.exitoFuerte,
                texto = stringResource(R.string.registro_demora_documento),
            )
            SeparadorTarjeta()
            FilaEstado(
                icono = painterResource(R.drawable.ic_reloj),
                tinte = colores.informacionFuerte,
                texto = stringResource(R.string.registro_demora_en_proceso),
            )
            SeparadorTarjeta()
            FilaEstado(
                icono = painterResource(R.drawable.ic_candado),
                tinte = colores.advertenciaFuerte,
                texto = stringResource(R.string.registro_demora_cuenta_inactiva),
            )
        }
    }
}

/** Medallón, titular y detalle: la cabecera común de las cuatro pantallas. */
@Composable
private fun androidx.compose.foundation.layout.ColumnScope.Encabezado(
    icono: Int,
    tinte: Color,
    fondo: Color,
    titulo: String,
    detalle: String,
) {
    val colores = LocalColoresSolventa.current
    MedallonEstado(
        icono = painterResource(icono),
        tinte = tinte,
        fondo = fondo,
        descripcion = titulo,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Text(text = titulo, style = MaterialTheme.typography.titleLarge)
    Text(
        text = detalle,
        style = MaterialTheme.typography.bodyMedium,
        color = colores.textoSecundario,
    )
}
