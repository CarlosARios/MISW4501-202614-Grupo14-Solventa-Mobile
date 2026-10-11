package co.solventa.movil.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import co.solventa.movil.ui.theme.Espacio

/**
 * Fila de una tarjeta de estado: un ícono con su color y el texto que explica
 * qué significa.
 *
 * El ícono no lleva descripción propia a propósito. El color y la forma nunca
 * son la única señal —el texto dice lo mismo—, así que anunciarlo por
 * separado solo haría que el lector de pantalla repita (OBJ 13). La fila se
 * lee como una sola unidad.
 */
@Composable
fun FilaEstado(
    icono: Painter,
    tinte: Color,
    texto: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacio.x12),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Espacio.x12)
            .clearAndSetSemantics { contentDescription = texto },
    ) {
        Icon(
            painter = icono,
            contentDescription = null,
            tint = tinte,
            modifier = Modifier.size(20.dp),
        )
        Text(text = texto, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Medallón de 96 dp con el ícono del resultado, como en M1d, M1e, M1g y M1h.
 */
@Composable
fun MedallonEstado(
    icono: Painter,
    tinte: Color,
    fondo: Color,
    descripcion: String,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(96.dp)
            .background(fondo, CircleShape),
    ) {
        Icon(
            painter = icono,
            contentDescription = descripcion,
            tint = tinte,
            modifier = Modifier.size(48.dp),
        )
    }
}

/** Bloque de consejos con viñeta, usado en M1a, M1b y M1e. */
@Composable
fun ListaConsejos(
    consejos: List<String>,
    icono: Painter,
    tinte: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Espacio.x8),
        modifier = modifier.fillMaxWidth(),
    ) {
        consejos.forEach { consejo ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Espacio.x8),
                modifier = Modifier.clearAndSetSemantics { contentDescription = consejo },
            ) {
                Icon(
                    painter = icono,
                    contentDescription = null,
                    tint = tinte,
                    modifier = Modifier.size(16.dp),
                )
                Text(text = consejo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
