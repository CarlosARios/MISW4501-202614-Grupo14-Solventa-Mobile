plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
}

android {
    namespace = "co.solventa.movil"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "co.solventa.movil"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Local: desarrollo diario contra el docker-compose del repositorio de
        // servicios. 10.0.2.2 es como el emulador alcanza el localhost del PC;
        // "localhost" dentro del emulador es el propio dispositivo virtual.
        debug {
            buildConfigField("String", "AMBIENTE", "\"local\"")
            buildConfigField("String", "API_GATEWAY_URL", "\"http://10.0.2.2:8080\"")
            buildConfigField("String", "COGNITO_REGION", "\"us-east-1\"")
            buildConfigField("String", "COGNITO_USER_POOL_ID", "\"local\"")
            buildConfigField("String", "COGNITO_CLIENTE_ID", "\"local\"")
        }

        // Producción (demo): la entrega evaluable que despliega el pipeline.
        // Los valores reales salen del repositorio de infraestructura (Terraform).
        release {
            buildConfigField("String", "AMBIENTE", "\"produccion\"")
            buildConfigField("String", "API_GATEWAY_URL", "\"PENDIENTE\"")
            buildConfigField("String", "COGNITO_REGION", "\"us-east-1\"")
            buildConfigField("String", "COGNITO_USER_POOL_ID", "\"PENDIENTE\"")
            buildConfigField("String", "COGNITO_CLIENTE_ID", "\"PENDIENTE\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }

        // Staging: réplica reducida que solo se enciende durante las ventanas
        // de experimentación (k6, Toxiproxy, OWASP ZAP). El sufijo del
        // applicationId permite tenerla instalada junto a la de producción.
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            matchingFallbacks += listOf("release")
            buildConfigField("String", "AMBIENTE", "\"staging\"")
            buildConfigField("String", "API_GATEWAY_URL", "\"PENDIENTE\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Compuerta de cobertura: la misma que el pipeline del cliente web (>= 70 %).
// Se excluye el código que generan Hilt, Room y Compose, y las vistas previas,
// porque no son código que el equipo pueda probar.
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.Hilt_*",
                    "*_HiltModules*",
                    "*_Factory",
                    "*_Factory\$*",
                    "*_MembersInjector",
                    "*_MembersInjector\$*",
                    "*_GeneratedInjector",
                    "hilt_aggregated_deps.*",
                    "dagger.hilt.*",
                    "*.ComposableSingletons*",
                    "*_Impl",
                    "*_Impl\$*",
                    // Capa de interfaz: se verifica con pruebas instrumentadas
                    // (Compose UI Test y Espresso), no con unitarias. Se
                    // excluyen los paquetes completos y no solo las funciones
                    // @Composable, porque un archivo de interfaz deja ademas
                    // constantes, enumeraciones y lambdas sueltas que no son
                    // logica de negocio y distorsionan la medicion.
                    "co.solventa.movil.ui.theme.*",
                    "co.solventa.movil.shared.ui.*",
                    "co.solventa.movil.MainActivity",
                    "co.solventa.movil.SolventaApp",
                    // Vistas previas: herramienta de diseno, no codigo que corra.
                    "*PreviewKt",
                    // Declaraciones de rutas: objetos sin comportamiento.
                    "co.solventa.movil.core.navegacion.Ruta",
                    "co.solventa.movil.core.navegacion.Ruta\$*",
                )
                annotatedBy(
                    "androidx.compose.ui.tooling.preview.Preview",
                    "androidx.compose.runtime.Composable",
                )
            }
        }
        verify {
            rule {
                minBound(70)
            }
        }
    }
}

// Deja el esquema de la base de datos versionado en app/schemas,
// para que las migraciones se puedan revisar en la solicitud de fusión.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navegación y estado
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Red
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.okhttp.logging)

    // Trabajo en segundo plano
    implementation(libs.androidx.work.runtime.ktx)

    // Cámara
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Biometría
    implementation(libs.androidx.biometric)

    // Inyección de dependencias
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Base de datos local
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}