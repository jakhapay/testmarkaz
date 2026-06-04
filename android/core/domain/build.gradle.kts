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

    // PDF text extraction for on-device test generation (domain/pdf)
    implementation(libs.pdfbox.android)

    // On-device LLM generation (optional — enable when shipping a Gemma/Qwen model).
    // See domain/pdf/LlmQuestionGenerator.kt. Until then the template generator runs.
    // implementation("com.google.mediapipe:tasks-genai:0.10.14")

    // Hilt for use-case injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Testing (PDF pipeline unit tests)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
