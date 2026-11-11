import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Fija Java 17 aunque Gradle se ejecute con otro JDK, para que :app y :data puedan consumir este módulo.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // api: Flow forma parte de la interfaz pública del dominio.
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
