package co.solventa.movil.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Escala de espaciado del prototipo (`espacio/…` en Figma). Toda separación,
 * relleno y margen sale de aquí: no se escriben valores sueltos en `dp`.
 */
object Espacio {
    val x4 = 4.dp
    val x8 = 8.dp
    val x12 = 12.dp
    val x16 = 16.dp
    val x24 = 24.dp
    val x32 = 32.dp
    val x48 = 48.dp
    val x64 = 64.dp
}

/** Radios del prototipo (`radio/…` en Figma). */
object Radio {
    val pequeno = 6.dp
    val base = 8.dp
    val tarjeta = 12.dp
    val completo = 999.dp
}

val FormasSolventa = Shapes(
    extraSmall = RoundedCornerShape(Radio.pequeno),
    small = RoundedCornerShape(Radio.base),
    medium = RoundedCornerShape(Radio.tarjeta),
    large = RoundedCornerShape(Radio.tarjeta),
    extraLarge = RoundedCornerShape(Radio.tarjeta),
)

/**
 * Colores semánticos que Material 3 no tiene dónde guardar.
 *
 * `ColorScheme` cubre primario, superficie y error, pero no éxito,
 * advertencia, información ni los dos tonos de borde, y la aplicación los
 * necesita en cada pantalla de estado. Se exponen por `LocalColoresSolventa`
 * para que una pantalla los lea igual que lee `MaterialTheme.colorScheme`.
 */
@Immutable
data class ColoresSolventa(
    val exitoFuerte: Color,
    val exito: Color,
    val exitoSuave: Color,
    val advertenciaFuerte: Color,
    val advertencia: Color,
    val advertenciaSuave: Color,
    val errorFuerte: Color,
    val errorSuave: Color,
    val informacionFuerte: Color,
    val informacion: Color,
    val informacionSuave: Color,
    val textoSecundario: Color,
    val bordeBase: Color,
    val bordeFuerte: Color,
    val lima: Color,
)

val coloresSolventaClaro = ColoresSolventa(
    exitoFuerte = Exito600,
    exito = Exito500,
    exitoSuave = Exito100,
    advertenciaFuerte = Advertencia600,
    advertencia = Advertencia500,
    advertenciaSuave = Advertencia100,
    errorFuerte = Error600,
    errorSuave = Error100,
    informacionFuerte = Informacion600,
    informacion = Informacion500,
    informacionSuave = Informacion100,
    textoSecundario = TextoSecundario,
    bordeBase = BordeBase,
    bordeFuerte = BordeFuerte,
    lima = Lima500,
)

val LocalColoresSolventa = staticCompositionLocalOf { coloresSolventaClaro }
