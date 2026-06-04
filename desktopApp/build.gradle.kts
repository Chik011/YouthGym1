import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.chiko0085.testgym.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.chiko0085.testgym"
            packageVersion = "1.0.0"
        }
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("android.arch.lifecycle:common")).using(module("androidx.lifecycle:lifecycle-common:2.8.4"))
        substitute(module("android.arch.lifecycle:runtime")).using(module("androidx.lifecycle:lifecycle-runtime:2.8.4"))
    }
}