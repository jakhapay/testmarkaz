plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace  = "uz.testmarkaz.core.domain"
    compileSdk = 35

    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Use cases query DAOs directly via injection
    implementation(project(":core:data"))

    implementation(libs.kotlinx.coroutines.android)

    // Hilt for use-case injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
