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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    // Kotlinx Coroutines (Kotlin library, not Android-specific)
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Dependency Injection - Using javax.inject (pure Java)
    compileOnly("javax.inject:javax.inject:1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.5")
}

