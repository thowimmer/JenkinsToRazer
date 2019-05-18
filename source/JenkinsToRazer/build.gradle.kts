import org.apache.tools.ant.taskdefs.condition.Os

val ktorVersion = "1.1.5"
val kotlinxSerializationVersion = "0.11.0"


plugins {
    id("kotlin-multiplatform") version "1.3.31"
    id("kotlinx-serialization") version "1.3.31"
}

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

kotlin {
    linuxX64("linux") {
        binaries {
            if(Os.isFamily(Os.FAMILY_UNIX)){
                executable()
            }
        }
    }

    mingwX64("windows") {
        binaries {
            executable()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-json:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVersion")
            }
        }

        val linuxMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("io.ktor:ktor-client-json-native:$ktorVersion")
            }
        }

        val windowsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("io.ktor:ktor-client-json-native:$ktorVersion")
            }
        }
    }
}