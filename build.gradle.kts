/*
 *  Copyright (c)  2021  Shabinder Singh
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    kotlin("multiplatform") version "1.4.32"
}

group = "com.shabinder"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "1.5.3"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                useIR = true
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        browser {
            testTask {
                useMocha {
                    timeout = "15000"
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "15000"
                }
            }
        }
    }
    ios()
    /*TODO:
     Native Targets (libCurl Interop issue)*/
    macosX64() { compilations.getByName("main").cinterops.create("curl")  }
    mingwX64() { compilations.getByName("main").cinterops.create("curl")  }
    //linuxX64() { compilations.getByName("main").cinterops.create("curl") }
    /*targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().forEach { target ->
        target.compilations.getByName("main").cinterops.create("curl")
    }*/
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt"){
                    version {
                        strictly("1.4.3-native-mt")
                    }
                }
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
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosTest by getting
        val desktopCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
            }
        }
        val desktopCommonTest by creating {
            dependsOn(commonTest)
        }
        val mingwX64Main by getting
        val macosX64Main by getting
//        val linuxX64Main by getting
        configure(listOf(mingwX64Main, macosX64Main/*, linuxX64Main*/)) {
            dependsOn(desktopCommonMain)
        }
        val mingwX64Test by getting
        val macosX64Test by getting
//        val linuxX64Test by getting
        configure(listOf(mingwX64Test, macosX64Test/*, linuxX64Test*/)) {
            dependsOn(desktopCommonTest)
        }
        /*val hostOs = System.getProperty("os.name")
        val isMingwX64 = hostOs.startsWith("Windows")
        val nativeTarget = when {
            hostOs == "Mac OS X" -> macosX64("native")
            hostOs == "Linux" -> linuxX64("native")
            isMingwX64 -> mingwX64("native")
            else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
        }*/
    }
}
