package co.solventa.movil.features.acceso.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.solventa.movil.R
import co.solventa.movil.shared.ui.BarraProgreso
import co.solventa.movil.shared.ui.BotonSolventa
import co.solventa.movil.shared.ui.ListaConsejos
import co.solventa.movil.shared.ui.PantallaRegistro
import co.solventa.movil.shared.ui.TipoBoton
import co.solventa.movil.ui.theme.Espacio
import co.solventa.movil.ui.theme.LocalColoresSolventa

/**
 * M1a y M1b · captura del frente y del reverso del documento.
 *
 * Son la misma pantalla con distinto texto, igual que en el prototipo. El
 * estado M1f —calidad insuficiente— se resuelve aquí dentro con una hoja
 * inferior, porque es un estado de esta pantalla y no un destino aparte.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturaDocumentoPantalla(
    cara: CaraDocumento,
    estado: RegistroUiState,
    onCapturar: () -> Unit,
    onDescartarAviso: () -> Unit,
    onAtras: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresSolventa.current
    val esFrente = cara == CaraDocumento.FRENTE

    PantallaRegistro(
        titulo = stringResource(R.string.registro_captura_titulo),
        onAtras = onAtras,
        modifier = modifier,
        acciones = {
            BotonSolventa(
                texto = stringResource(R.string.registro_captura_accion),
                onClick = onCapturar,
                icono = painterResource(R.drawable.ic_camara),
                habilitado = !estado.analizando,
            )
            BotonSolventa(
                texto = stringResource(R.string.registro_captura_problemas),
                onClick = onAtras,
                tipo = TipoBoton.TERCIARIO,
            )
        },
    ) {
        PasoDelRegistro(
            avance = 1f / 3f,
            etiqueta = stringResource(R.string.registro_captura_paso),
        )

        Text(
            text = stringResource(
                if (esFrente) {
                    R.string.registro_captura_frente_instruccion
                } else {
                    R.string.registro_captura_reverso_instruccion
                },
            ),
            style = MaterialTheme.typography.titleLarge,
        )

        MarcoDocumento(
            etiqueta = stringResource(
                if (esFrente) {
                    R.string.registro_captura_frente_etiqueta
                } else {
                    R.string.registro_captura_reverso_etiqueta
                },
            ),
        )

        Text(
            text = stringResource(
                if (esFrente) {
                    R.string.registro_captura_frente_ayuda
                } else {
                    R.string.registro_captura_reverso_ayuda
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = colores.textoSecundario,
        )

        ListaConsejos(
            consejos = listOf(
                stringResource(R.string.registro_captura_consejo_luz),
                stringResource(R.string.registro_captura_consejo_reflejos),
                stringResource(R.string.registro_captura_consejo_nitidez),
            ),
            icono = painterResource(R.drawable.ic_check),
            tinte = colores.exitoFuerte,
        )

        Text(
            text = stringResource(R.string.registro_captura_privacidad),
            style = MaterialTheme.typography.bodySmall,
            color = colores.textoSecundario,
        )
    }

    val problema = estado.problemaCalidad
    if (problema != null) {
        ModalBottomSheet(onDismissRequest = onDescartarAviso) {
            HojaCalidadInsuficiente(problema = problema, onReintentar = onDescartarAviso)
        }
    }
}

/** M1f · la hoja que explica por qué la imagen no sirve y cómo corregirlo. */
@Composable
private fun HojaCalidadInsuficiente(
    problema: ProblemaCalidad,
    onReintentar: () -> Unit,
) {
    val colores = LocalColoresSolventa.current
    val (tituloRes, detalleRes) = when (problema) {
        ProblemaCalidad.ILUMINACION_INSUFICIENTE ->
            R.string.registro_calidad_oscura_titulo to R.string.registro_calidad_oscura_detalle

        ProblemaCalidad.IMAGEN_BORROSA ->
            R.string.registro_calidad_borrosa_titulo to R.string.registro_calidad_borrosa_detalle

        ProblemaCalidad.DOCUMENTO_INCOMPLETO ->
            R.string.registro_calidad_incompleta_titulo to R.string.registro_calidad_incompleta_detalle
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.x12),
        modifier = Modifier.padding(
            start = Espacio.x16,
            end = Espacio.x16,
            bottom = Espacio.x32,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espacio.x12),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_triangulo_alerta),
                contentDescription = null,
                tint = colores.advertenciaFuerte,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(tituloRes),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = stringResource(detalleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = colores.textoSecundario,
        )
        BotonSolventa(
            texto = stringResource(R.string.registro_calidad_reintentar),
            onClick = onReintentar,
        )
    }
}

/**
 * Marco guía donde se ubica el documento. La proporción es la de una cédula
 * colombiana; cuando entre CameraX, aquí va la vista previa de la cámara.
 */
@Composable
private fun MarcoDocumento(etiqueta: String, modifier: Modifier = Modifier) {
    val colores = LocalColoresSolventa.current
    val descripcion = stringResource(R.string.registro_captura_marco_descripcion)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(co.solventa.movil.ui.theme.Radio.tarjeta),
            )
            .border(
                2.dp,
                MaterialTheme.colorScheme.secondary,
                RoundedCornerShape(co.solventa.movil.ui.theme.Radio.tarjeta),
            )
            .clearAndSetSemantics { contentDescription = descripcion },
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = colores.textoSecundario,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** Barra de avance con la etiqueta del paso, común a las ocho pantallas. */
@Composable
internal fun PasoDelRegistro(
    avance: Float,
    etiqueta: String,
    color: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresSolventa.current
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.x8),
        modifier = modifier.fillMaxWidth(),
    ) {
        BarraProgreso(avance = avance, color = color)
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = colores.textoSecundario,
        )
    }
}

/** M1c · confirmación de los datos que el proveedor extrajo del documento. */
@Composable
fun ConfirmacionDatosPantalla(
    estado: RegistroUiState,
    onDatosCambian: (DatosDocumento) -> Unit,
    onTerminos: (Boolean) -> Unit,
    onVerificar: () -> Unit,
    onAtras: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = LocalColoresSolventa.current
    val datos = estado.datos ?: return

    PantallaRegistro(
        titulo = stringResource(R.string.registro_datos_titulo),
        onAtras = onAtras,
        modifier = modifier,
        acciones = {
            BotonSolventa(
                texto = stringResource(R.string.registro_datos_accion),
                onClick = onVerificar,
                habilitado = estado.puedeVerificar,
            )
        },
    ) {
        PasoDelRegistro(
            avance = 2f / 3f,
            etiqueta = stringResource(R.string.registro_datos_paso),
        )

        Text(
            text = stringResource(R.string.registro_datos_introduccion),
            style = MaterialTheme.typography.bodyMedium,
            color = colores.textoSecundario,
        )

        CampoDato(
            etiqueta = stringResource(R.string.registro_datos_nombres),
            valor = datos.nombres,
            onCambio = { onDatosCambian(datos.copy(nombres = it)) },
        )
        CampoDato(
            etiqueta = stringResource(R.string.registro_datos_apellidos),
            valor = datos.apellidos,
            onCambio = { onDatosCambian(datos.copy(apellidos = it)) },
        )
        CampoDato(
            etiqueta = stringResource(R.string.registro_datos_documento),
            valor = "${datos.tipoDocumento} ${datos.numeroDocumento}",
            onCambio = {},
            soloLectura = true,
            ayuda = stringResource(R.string.registro_datos_documento_ayuda),
        )
        CampoDato(
            etiqueta = stringResource(R.string.registro_datos_nacimiento),
            valor = datos.fechaNacimiento,
            onCambio = { onDatosCambian(datos.copy(fechaNacimiento = it)) },
            ayuda = stringResource(R.string.registro_datos_nacimiento_formato),
        )
        CampoDato(
            etiqueta = stringResource(R.string.registro_datos_correo),
            valor = datos.correo,
            onCambio = { onDatosCambian(datos.copy(correo = it)) },
        )
        CampoDato(
            etiqueta = stringResource(R.string.registro_datos_celular),
            valor = datos.celular,
            onCambio = { onDatosCambian(datos.copy(celular = it)) },
            ayuda = stringResource(R.string.registro_datos_celular_ayuda),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espacio.x8),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(checked = estado.terminosAceptados, onCheckedChange = onTerminos)
            Text(
                text = stringResource(R.string.registro_datos_terminos),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CampoDato(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    soloLectura: Boolean = false,
    ayuda: String? = null,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        readOnly = soloLectura,
        singleLine = true,
        supportingText = ayuda?.let { { Text(it) } },
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
    )
}
