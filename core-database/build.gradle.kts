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

    // Dependency Injection - Using javax.inject (pure Java)
    compileOnly(libs.javax.inject)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

