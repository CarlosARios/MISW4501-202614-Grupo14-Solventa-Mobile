package co.solventa.movil.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Alcance de corrutinas que vive tanto como la aplicación.
 *
 * Lo usan los componentes que tienen que seguir trabajando cuando la pantalla
 * que los disparó ya no existe —el temporizador de inactividad, la cola de
 * sincronización—. `SupervisorJob` evita que el fallo de uno cancele a los
 * demás.
 */
@Module
@InstallIn(SingletonComponent::class)
object CorrutinasModule {

    @Provides
    @Singleton
    fun proveerAlcanceDeAplicacion(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
