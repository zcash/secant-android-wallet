plugins {
    kotlin("multiplatform")
    id("zcash.kotlin-multiplatform-build-conventions")
    id("zcash.kotlin-multiplatform-jacoco-conventions")
}

kotlin {
    jvm()
    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.coroutines.core)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        getByName("jvmMain") {
            dependencies {
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
