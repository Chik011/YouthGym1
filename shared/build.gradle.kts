import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    androidLibrary {
        namespace = "com.chiko0085.testgym.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebaseCommonKtxGoogle)
            implementation(libs.firebaseFirestoreGoogle)
            implementation(libs.codescanner)

            // Peekaboo khusus untuk Android
            implementation("io.github.onseok:peekaboo-image-picker:0.5.2")
            implementation(libs.androidx.foundation)
        }

        val iosMain = maybeCreate("iosMain")
        iosMain.dependencies {
            implementation("io.github.onseok:peekaboo-image-picker:0.5.2")
        }
        maybeCreate("iosArm64Main").dependsOn(iosMain)
        maybeCreate("iosSimulatorArm64Main").dependsOn(iosMain)
        // ---------------------------------------------

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation("com.russhwolf:multiplatform-settings:1.1.1")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Kamel dan Firebase Storage tetap di commonMain
            implementation("media.kamel:kamel-image:0.9.5")
            implementation("dev.gitlive:firebase-storage:1.10.4")

            api(libs.firebase.firestore)
            api(libs.firebase.common)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("android.arch.lifecycle:common")).using(module("androidx.lifecycle:lifecycle-common:2.8.4"))
        substitute(module("android.arch.lifecycle:runtime")).using(module("androidx.lifecycle:lifecycle-runtime:2.8.4"))
    }
}