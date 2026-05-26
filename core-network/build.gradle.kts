plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Kotlin Standard Library
    implementation(libs.kotlin.stdlib.jdk8)

    // Kotlinx Coroutines (Kotlin library, not Android-specific)
    api(libs.kotlinx.coroutines.test)

    // Retrofit & Networking (Android-agnostic)
    api(libs.retrofit.new)
    api(libs.retrofit.converter.gson.new)
    api(libs.okhttp)
    api(libs.logging.interceptor)

    // Gson for JSON serialization (Kotlin library)
    api(libs.gson.new)

    // Dependency Injection - Using javax.inject (pure Java)
    compileOnly(libs.javax.inject)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

