plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    // TAMBAHKAN BARIS INI:
    id("com.google.gms.google-services") version "4.4.4" apply false
}

subprojects {
    dependencies {
        modules {
            module("android.arch.lifecycle:common") {
                replacedBy("androidx.lifecycle:lifecycle-common", "Conflict with AndroidX")
            }
            module("android.arch.lifecycle:runtime") {
                replacedBy("androidx.lifecycle:lifecycle-runtime", "Conflict with AndroidX")
            }
        }
    }
}