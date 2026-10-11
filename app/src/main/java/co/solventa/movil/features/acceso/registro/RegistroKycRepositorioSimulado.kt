package co.solventa.movil.features.acceso.registro

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación temporal del repositorio de registro.
 *
 * **Por qué existe:** el proveedor de verificación de identidad es una
 * decisión de implementación que la historia deja abierta, el contrato del
 * API todavía no está publicado y `core/api/` no tiene cliente para este
 * flujo. Esta clase permite construir y probar el recorrido completo sin
 * backend.
 *
 * **Se comporta como un guion**, no como un camino feliz, para que los ocho
 * frames se puedan recorrer tocando la pantalla en la demostración del
 * sprint:
 *
 * | Momento | Resultado | Pantalla que se muestra |
 * |---|---|---|
 * | Primera captura del frente | Iluminación insuficiente | M1f |
 * | Capturas siguientes | Aceptada | M1a → M1b → M1c |
 * | Primera verificación | No superada | M1e |
 * | Verificaciones siguientes | Exitosa | M1d |
 *
 * Los contadores viven en el singleton, así que sobreviven al `reiniciar()`
 * del `ViewModel`: después de *Intentar de nuevo* el recorrido fluye hasta el
 * final. M1g y M1h se alcanzan desde la propia espera.
 *
 * **Qué la reemplaza:** un cliente Retrofit en `core/api/<servicio>` cuando el
 * equipo defina dónde vive el endpoint de registro. El resto del flujo no
 * cambia: la interfaz [RegistroKycRepositorio] es la misma. Esta clase y sus
 * pruebas se borran juntas.
 *
 * Los datos y la referencia son los de los frames M1c y M1e, para que la
 * demostración coincida con el prototipo.
 */
@Singleton
class RegistroKycRepositorioSimulado @Inject constructor() : RegistroKycRepositorio {

    private val intentosDelFrente = AtomicInteger(0)
    private val intentosDeVerificacion = AtomicInteger(0)

    override suspend fun analizarCaptura(imagen: ImagenDocumento): ResultadoCaptura {
        delay(DEMORA_ANALISIS_MS)
        return when (imagen.cara) {
            CaraDocumento.FRENTE ->
                if (intentosDelFrente.incrementAndGet() == 1) {
                    ResultadoCaptura.CalidadInsuficiente(ProblemaCalidad.ILUMINACION_INSUFICIENTE)
                } else {
                    ResultadoCaptura.Aceptada(datos = null)
                }

            CaraDocumento.REVERSO -> ResultadoCaptura.Aceptada(datos = DATOS_DE_EJEMPLO)
        }
    }

    override suspend fun verificarIdentidad(datos: DatosDocumento): ResultadoVerificacion {
        delay(DEMORA_VERIFICACION_MS)
        return if (intentosDeVerificacion.incrementAndGet() == 1) {
            ResultadoVerificacion.NoSuperada(referencia = REFERENCIA_DE_EJEMPLO)
        } else {
            ResultadoVerificacion.Exitosa
        }
    }

    private companion object {
        const val DEMORA_ANALISIS_MS = 1_200L
        const val DEMORA_VERIFICACION_MS = 3_000L
        const val REFERENCIA_DE_EJEMPLO = "KYC-2026-77A2 · 16/09/2026 15:20"

        val DATOS_DE_EJEMPLO = DatosDocumento(
            nombres = "Carolina",
            apellidos = "Pérez Ruiz",
            tipoDocumento = "CC",
            numeroDocumento = "1.020.345.678",
            fechaNacimiento = "14/05/1991",
            correo = "carolina.perez@correo.com",
            celular = "+57 301 234 5678",
        )
    }
}
