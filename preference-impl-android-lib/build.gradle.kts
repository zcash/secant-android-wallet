plugins {
    id("com.android.library")
    kotlin("android")
    id("secant.android-build-conventions")
    id("wtf.emulator.gradle")
    id("secant.emulator-wtf-conventions")
}

// Force orchestrator to be used for this module, because we need the preference files
// to be purged between tests
val isOrchestratorEnabled = true

android {
    if (isOrchestratorEnabled) {
        defaultConfig {
            testInstrumentationRunnerArguments["clearPackageData"] = "true"
        }

        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }
    }
}

dependencies {
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.preferenceApiLib)

    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    if (isOrchestratorEnabled) {
        androidTestUtil(libs.androidx.test.orchestrator) {
            artifact {
                type = "apk"
            }
        }
    }
}
