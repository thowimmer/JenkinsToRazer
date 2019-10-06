import org.apache.tools.ant.taskdefs.condition.Os

val ktorVersion = "1.2.2"
val kotlinxSerializationVersion = "0.11.1"
val kotlinxCoroutinesVersion = "1.3.0-M2"

plugins {
    id("kotlin-multiplatform") version "1.3.40"
    id("kotlinx-serialization") version "1.3.40"
    id("io.github.robwin.jgitflow") version "0.6.0"
}

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/robwin") }
}

kotlin {
    linuxX64("linux") {
        binaries {
            if(Os.isFamily(Os.FAMILY_UNIX)){
                executable{
                    baseName = "JenkinsToRazer_$version"
                }
            }
        }
    }

    mingwX64("windows") {
        binaries {
            executable{
                baseName = "JenkinsToRazer_$version"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$kotlinxCoroutinesVersion")
            }
        }

        val linuxMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-linuxx64:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-linuxx64:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-linuxx64:$kotlinxCoroutinesVersion")
            }
        }

        val windowsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-mingwx64:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-mingwx64:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-windowsx64:$kotlinxCoroutinesVersion")
            }
        }
    }
}