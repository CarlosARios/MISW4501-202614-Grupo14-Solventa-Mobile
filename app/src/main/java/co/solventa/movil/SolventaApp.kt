package co.solventa.movil

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de la aplicación. La anotación le indica a Hilt que genere
 * aquí el contenedor raíz de dependencias, del que cuelgan los demás.
 */
@HiltAndroidApp
class SolventaApp : Application()
