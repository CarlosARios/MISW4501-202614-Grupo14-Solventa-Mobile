package co.solventa.movil.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * El prototipo tiene un solo modo, `Claro`. Por eso aquí no hay esquema
 * oscuro ni color dinámico de Material You: una pantalla en modo oscuro
 * perdería el contraste AA que se verificó en el diseño, y un color tomado
 * del fondo de pantalla del usuario dejaría de ser la marca.
 *
 * Cuando el equipo diseñe el modo oscuro, se agrega un segundo modo a la
 * colección de Figma y un `darkColorScheme` aquí, no antes.
 */
private val EsquemaClaro = lightColorScheme(
    primary = Azul700,
    onPrimary = TextoInverso,
    primaryContainer = Azul100,
    onPrimaryContainer = Azul900,

    secondary = Azul500,
    onSecondary = TextoInverso,
    secondaryContainer = Azul50,
    onSecondaryContainer = Azul900,

    tertiary = Lima500,
    onTertiary = TextoPrimario,

    background = FondoPagina,
    onBackground = TextoPrimario,
    surface = FondoSuperficie,
    onSurface = TextoPrimario,
    surfaceVariant = Azul50,
    onSurfaceVariant = TextoSecundario,

    error = Error500,
    onError = TextoInverso,
    errorContainer = Error100,
    onErrorContainer = Error600,

    outline = BordeFuerte,
    outlineVariant = BordeBase,
)

@Composable
fun SolventaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalColoresSolventa provides coloresSolventaClaro) {
        MaterialTheme(
            colorScheme = EsquemaClaro,
            typography = Typography,
            shapes = FormasSolventa,
            content = content,
        )
    }
}
