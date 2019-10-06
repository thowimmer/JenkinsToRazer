enableFeaturePreview("GRADLE_METADATA")

pluginManagement {
    repositories {
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "http://kotlin.bintray.com/kotlin-eap/")
        maven(url = "http://kotlin.bintray.com/kotlinx/")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin-multiplatform") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
            if (requested.id.id == "io.github.robwin.jgitflow") {
                useModule("io.github.robwin:jgitflow-gradle-plugin:${requested.version}")
            }
        }
    }
}