plugins {
    kotlin("multiplatform") version "1.3.31"
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("linux") {
        binaries {
            executable()
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
            }
        }

        linuxX64("linux").compilations["main"].defaultSourceSet  { /* ... */ }
        mingwX64("windows").compilations["main"].defaultSourceSet  { /* ... */ }
    }
}