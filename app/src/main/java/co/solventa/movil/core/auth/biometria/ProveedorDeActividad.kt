package co.solventa.movil.core.auth.biometria

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda la actividad en pantalla para quien la necesite.
 *
 * `BiometricPrompt` exige una `FragmentActivity` porque se monta como un
 * fragmento, y eso no se puede inyectar: una actividad nace y muere muchas
 * veces durante la vida de la aplicación. La referencia es **débil** para que
 * una actividad destruida —al rotar el teléfono, por ejemplo— no quede
 * retenida por este singleton.
 *
 * Es la única pieza de `core` que conoce una actividad, y existe solo por esa
 * exigencia de la biblioteca de biometría.
 */
@Singleton
class ProveedorDeActividad @Inject constructor() {

    private var referencia: WeakReference<FragmentActivity>? = null

    fun registrar(actividad: FragmentActivity) {
        referencia = WeakReference(actividad)
    }

    fun olvidar() {
        referencia = null
    }

    fun actual(): FragmentActivity? = referencia?.get()
}
