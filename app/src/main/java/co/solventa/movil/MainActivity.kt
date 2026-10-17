package co.solventa.movil

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import co.solventa.movil.core.auth.biometria.ProveedorDeActividad
import co.solventa.movil.ui.theme.SolventaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Extiende `FragmentActivity` y no `ComponentActivity` porque
 * `BiometricPrompt` se monta como un fragmento y lo exige. `FragmentActivity`
 * es una `ComponentActivity`, así que Compose funciona igual.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var actividades: ProveedorDeActividad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actividades.registrar(this)
        enableEdgeToEdge()
        setContent {
            SolventaTheme {
                SolventaNavHost()
            }
        }
    }

    override fun onDestroy() {
        actividades.olvidar()
        super.onDestroy()
    }
}
