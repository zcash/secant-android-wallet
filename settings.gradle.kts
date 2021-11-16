enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    plugins {
        val detektVersion = extra["DETEKT_VERSION"].toString()
        val gradleVersionsPluginVersion = extra["GRADLE_VERSIONS_PLUGIN_VERSION"].toString()
        val kotlinVersion = extra["KOTLIN_VERSION"].toString()

        kotlin("jvm") version (kotlinVersion)
        kotlin("multiplatform") version (kotlinVersion)
        id("com.github.ben-manes.versions") version (gradleVersionsPluginVersion) apply (false)
        id("io.gitlab.arturbosch.detekt") version (detektVersion) apply (false)
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        val isRepoRestrictionEnabled = true

        maven("https://dl.google.com/dl/android/maven2/") { // google()
            if (isRepoRestrictionEnabled) {
                content {
                    includeGroup("android.arch.lifecycle")
                    includeGroup("android.arch.core")
                    includeGroup("com.google.android.material")
                    includeGroupByRegex("androidx.*")
                    includeGroupByRegex("com\\.android.*")
                }
            }
        }
        maven("https://repo.maven.apache.org/maven2/") { // mavenCentral()
            if (isRepoRestrictionEnabled) {
                content {
                    excludeGroup("android.arch.lifecycle")
                    excludeGroup("android.arch.core")
                    excludeGroup("com.google.android.material")
                    excludeGroupByRegex("androidx.*")
                    excludeGroupByRegex("com\\.android.*")
                }
            }
        }
    }

    @Suppress("UnstableApiUsage", "MaxLineLength")
    versionCatalogs {
        create("libs") {
            val androidxActivityVersion = extra["ANDROIDX_ACTIVITY_VERSION"].toString()
            val androidxAnnotationVersion = extra["ANDROIDX_ANNOTATION_VERSION"].toString()
            val androidxAppcompatVersion = extra["ANDROIDX_APPCOMPAT_VERSION"].toString()
            val androidxComposeVersion = extra["ANDROIDX_COMPOSE_VERSION"].toString()
            val androidxComposeCompilerVersion = extra["ANDROIDX_COMPOSE_COMPILER_VERSION"].toString()
            val androidxCoreVersion = extra["ANDROIDX_CORE_VERSION"].toString()
            val androidxEspressoVersion = extra["ANDROIDX_ESPRESSO_VERSION"].toString()
            val androidxLifecycleVersion = extra["ANDROIDX_LIFECYCLE_VERSION"].toString()
            val androidxTestJunitVersion = extra["ANDROIDX_TEST_JUNIT_VERSION"].toString()
            val androidxTestOrchestratorVersion = extra["ANDROIDX_TEST_ORCHESTRATOR_VERSION"].toString()
            val androidxTestVersion = extra["ANDROIDX_TEST_VERSION"].toString()
            val androidxUiAutomatorVersion = extra["ANDROIDX_UI_AUTOMATOR_VERSION"].toString()
            val androidxSecurityCryptoVersion = extra["ANDROIDX_SECURITY_CRYPTO_VERSION"].toString()
            val coreLibraryDesugaringVersion = extra["CORE_LIBRARY_DESUGARING_VERSION"].toString()
            val googleMaterialVersion = extra["GOOGLE_MATERIAL_VERSION"].toString()
            val jacocoVersion = extra["JACOCO_VERSION"].toString()
            val javaVersion = extra["ANDROID_JVM_TARGET"].toString()
            val kotlinVersion = extra["KOTLIN_VERSION"].toString()
            val kotlinxCoroutinesVersion = extra["KOTLINX_COROUTINES_VERSION"].toString()
            val kotlinxDatetimeVersion = extra["KOTLINX_DATETIME_VERSION"].toString()
            val zcashSdkVersion = extra["ZCASH_SDK_VERSION"].toString()
            val zcashBip39Version = extra["ZCASH_BIP39_VERSION"].toString()

            // Standalone versions
            version("jacoco", jacocoVersion)
            version("java", javaVersion)

            // Aliases
            alias("androidx-activity").to("androidx.activity:activity-ktx:$androidxActivityVersion")
            alias("androidx-activity-compose").to("androidx.activity:activity-compose:$androidxActivityVersion")
            alias("androidx-annotation").to("androidx.annotation:annotation:$androidxAnnotationVersion")
            alias("androidx-appcompat").to("androidx.appcompat:appcompat:$androidxAppcompatVersion")
            alias("androidx-compose-foundation").to("androidx.compose.foundation:foundation:$androidxComposeVersion")
            alias("androidx-compose-material").to("androidx.compose.material:material:$androidxComposeVersion")
            alias("androidx-compose-material-icons-core").to("androidx.compose.material:material-icons-core:$androidxComposeVersion")
            alias("androidx-compose-material-icons-extended").to("androidx.compose.material:material-icons-extended:$androidxComposeVersion")
            alias("androidx-compose-tooling").to("androidx.compose.ui:ui-tooling:$androidxComposeVersion")
            alias("androidx-compose-ui").to("androidx.compose.ui:ui:$androidxComposeVersion")
            alias("androidx-compose-compiler").to("androidx.compose.compiler:compiler:$androidxComposeCompilerVersion")
            alias("androidx-core").to("androidx.core:core-ktx:$androidxCoreVersion")
            alias("androidx-lifecycle-livedata").to("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")
            alias("androidx-security-crypto").to("androidx.security:security-crypto-ktx:$androidxSecurityCryptoVersion")
            alias("androidx-viewmodel-compose").to("androidx.lifecycle:lifecycle-viewmodel-compose:$androidxLifecycleVersion")
            alias("desugaring").to("com.android.tools:desugar_jdk_libs:$coreLibraryDesugaringVersion")
            alias("google-material").to("com.google.android.material:material:$googleMaterialVersion")
            alias("kotlin-stdlib").to("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
            alias("kotlin-reflect").to("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            alias("kotlinx-coroutines-android").to("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutinesVersion")
            alias("kotlinx-coroutines-core").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
            alias("kotlinx-datetime").to("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
            alias("zcash-sdk").to("cash.z.ecc.android:zcash-android-sdk:$zcashSdkVersion")
            alias("zcash-bip39").to("cash.z.ecc.android:kotlin-bip39:$zcashBip39Version")
            alias("zcash-walletplgns").to("cash.z.ecc.android:zcash-android-wallet-plugins:$zcashBip39Version")
            // Test libraries
            alias("androidx-compose-test-junit").to("androidx.compose.ui:ui-test-junit4:$androidxComposeVersion")
            alias("androidx-compose-test-manifest").to("androidx.compose.ui:ui-test-manifest:$androidxComposeVersion")
            // Cannot use espresso-contrib, because it causes a build failure
            //alias("androidx-espresso-contrib").to("androidx.test.espresso:espresso-contrib:$androidxEspressoVersion")
            alias("androidx-espresso-core").to("androidx.test.espresso:espresso-core:$androidxEspressoVersion")
            alias("androidx-espresso-intents").to("androidx.test.espresso:espresso-intents:$androidxEspressoVersion")
            alias("androidx-test-core").to("androidx.test:core:$androidxTestVersion")
            alias("androidx-test-junit").to("androidx.test.ext:junit:$androidxTestJunitVersion")
            alias("androidx-test-orchestrator").to("androidx.test:orchestrator:$androidxTestOrchestratorVersion")
            alias("androidx-test-runner").to("androidx.test:runner:$androidxTestVersion")
            alias("androidx-uiAutomator").to("androidx.test.uiautomator:uiautomator-v18:$androidxUiAutomatorVersion")
            alias("kotlinx-coroutines-test").to("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")

            // Bundles
            bundle(
                "androidx-compose",
                listOf(
                    "androidx-activity-compose",
                    "androidx-compose-compiler",
                    "androidx-compose-foundation",
                    "androidx-compose-material",
                    "androidx-compose-material-icons-core",
                    "androidx-compose-material-icons-extended",
                    "androidx-compose-tooling",
                    "androidx-compose-ui",
                    "androidx-viewmodel-compose"
                )
            )
            bundle(
                "androidx-test",
                listOf(
                    "androidx-espresso-core",
                    "androidx-espresso-intents",
                    "androidx-test-core",
                    "androidx-test-junit",
                    "androidx-test-runner"
                )
            )
        }
    }
}

rootProject.name = "zcash-android-app"

includeBuild("build-conventions")

include("app")
include("build-info-lib")
include("configuration-api-lib")
include("preference-api-lib")
include("preference-impl-android-lib")
include("sdk-ext-lib")
include("test-lib")
include("ui-lib")
