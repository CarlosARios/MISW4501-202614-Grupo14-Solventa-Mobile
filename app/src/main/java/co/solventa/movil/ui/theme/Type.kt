package co.solventa.movil.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * El prototipo usa **Inter**. Mientras el equipo decide cómo incorporarla
 * —empaquetar los archivos en `res/font/` o resolverla con
 * `ui-text-google-fonts`— se usa la familia sin serifa del sistema, que en
 * Android es Roboto. Cambiarlo después es editar solo esta constante.
 */
private val FamiliaSolventa = FontFamily.SansSerif

/**
 * Escala tipográfica del prototipo. Los tamaños y pesos corresponden a los
 * de los frames móviles de 390 × 844.
 */
val Typography = Typography(
    // Título de pantalla: "Verificando identidad", "Mis siniestros"
    headlineSmall = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    // Titular dentro del contenido: "Estamos verificando tu identidad"
    titleLarge = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    // Encabezado de tarjeta o sección
    titleMedium = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    // Etiqueta de campo
    titleSmall = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // Cuerpo principal
    bodyLarge = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Cuerpo secundario y descripciones
    bodyMedium = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // Texto de apoyo: "Paso 3 de 3 · Verificación"
    bodySmall = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    // Texto de botón
    labelLarge = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Insignias de estado
    labelMedium = TextStyle(
        fontFamily = FamiliaSolventa,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
