plugins {
    kotlin("multiplatform") version "1.4.31"
}

group = "com.shabinder"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://cache-redirector.jetbrains.com/maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(LEGACY) {
        browser {
            testTask {
                useKarma {
                    //useChromeHeadless()
                    useFirefox()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    val ktorVersion = "1.5.2"
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val nativeTest by getting
    }
}
