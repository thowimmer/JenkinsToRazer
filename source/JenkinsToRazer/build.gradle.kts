plugins {
    kotlin("multiplatform") version "1.3.31"
}

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

kotlin {
    linuxX64("linux") {
        binaries {
            executable()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("io.ktor:ktor-client-core:1.1.5")
            }
        }

        val linuxMain by getting {
            dependsOn(commonMain)
            dependencies {
                api("io.ktor:ktor-client-curl:1.1.5")
            }
        }
    }
}