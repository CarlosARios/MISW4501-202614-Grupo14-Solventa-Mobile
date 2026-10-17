package co.solventa.movil.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import co.solventa.movil.R
import androidx.compose.ui.res.stringResource
import co.solventa.movil.ui.theme.Espacio
import co.solventa.movil.ui.theme.LocalColoresSolventa

/**
 * Logotipo de la marca: el nombre en azul con el punto lima.
 *
 * Se lee como una sola unidad para el lector de pantalla; el punto es
 * decorativo y no se anuncia aparte.
 */
@Composable
fun LogotipoSolventa(modifier: Modifier = Modifier) {
    val nombre = stringResource(R.string.bienvenida_titulo)
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.clearAndSetSemantics { contentDescription = nombre },
    ) {
        Text(
            text = nombre,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Box(
            modifier = Modifier
                .padding(start = Espacio.x4, bottom = Espacio.x4)
                .size(6.dp)
                .background(LocalColoresSolventa.current.lima, CircleShape),
        )
    }
}
