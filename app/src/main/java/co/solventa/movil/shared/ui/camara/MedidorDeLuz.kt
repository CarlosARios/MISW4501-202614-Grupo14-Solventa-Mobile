package co.solventa.movil.shared.ui.camara

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * Mide la luminancia media de lo que ve la cámara.
 *
 * Es lo que sostiene el criterio 4 de HU-M1.1-01: avisar de la poca luz
 * **durante la captura**, no después de enviar la foto. Antes esto lo fingía
 * el repositorio simulado, que declaraba oscura la primera captura aunque la
 * habitación estuviera iluminada.
 *
 * Solo se lee el plano Y del formato YUV_420_888, que **es** la luminancia:
 * no hay que convertir a RGB ni recorrer los tres planos. Y se muestrea uno
 * de cada [SALTO] bytes, porque para decidir «está oscuro» sobra con unos
 * miles de muestras y así el análisis no compite con la vista previa.
 */
internal class MedidorDeLuz(
    private val alMedir: (luminanciaMedia: Int) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(imagen: ImageProxy) {
        imagen.use {
            val plano = it.planes.firstOrNull() ?: return@use
            val bufer = plano.buffer
            var suma = 0L
            var muestras = 0
            var i = bufer.position()
            val fin = bufer.limit()
            while (i < fin) {
                // El byte es con signo y la luminancia va de 0 a 255.
                suma += bufer.get(i).toInt() and 0xFF
                muestras++
                i += SALTO
            }
            if (muestras > 0) alMedir((suma / muestras).toInt())
        }
    }

    internal companion object {
        private const val SALTO = 64

        /**
         * Por debajo de este valor se considera que no hay luz suficiente.
         *
         * Calibrado contra el Moto G20: con el lente tapado la media queda
         * cerca de 10, y un documento sobre un escritorio con luz de techo
         * ronda 110. El umbral se deja holgado hacia abajo para no molestar a
         * quien está capturando en condiciones apenas correctas: el aviso
         * tiene que aparecer cuando la foto de verdad no va a servir, no cada
         * vez que la luz no es de estudio.
         */
        const val UMBRAL_DE_OSCURIDAD = 45

        /** Si una luminancia media cae por debajo del umbral. */
        internal fun esOscuro(luminancia: Int): Boolean = luminancia < UMBRAL_DE_OSCURIDAD
    }
}
