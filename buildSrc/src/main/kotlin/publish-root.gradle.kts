import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

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
    id("maven-publish")
    id("signing")
}

afterEvaluate {

    publishing {
        repositories {
            maven {
                name = "nexus"
                url = uri("https://s01.oss.sonatype.org/service/local/")
                //url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = "OSS_USERNAME".byProperty
                    password = "OSS_PASSWORD".byProperty
                }
            }
            maven {
                name = "snapshot"
                url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                credentials {
                    username = "OSS_USERNAME".byProperty
                    password = "OSS_PASSWORD".byProperty
                }
            }
        }

        publications {
            create<MavenPublication>("release") {
                if (plugins.hasPlugin("com.android.library")) {
                    from(components["release"])
                } else {
                    from(components["java"])
                }
//                artifact(dokkaJar)
//                artifact(sourcesJar)

                pom {
                    withXml {
                        asNode().apply {
                            appendNode("name", "youtube-api-dl")
                            appendNode("description", "Multiplatform Youtube Metadata Retriever and Downloader")
                            appendNode("url", "https://github.com/Shabinder/youtube-api-dl/")
                        }
                    }
                    if (!"USE_SNAPSHOT".byProperty.isNullOrBlank()) {
                        version = "$version-SNAPSHOT"
                    }
                    description.set("Multiplatform Youtube Metadata Retriever and Downloader")
                    url.set("https://github.com/Shabinder/youtube-api-dl/")

                    licenses {
                        license {
                            name.set("GPL-3.0 License")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }
                    developers {
                        developer {
                            id.set("shabinder")
                            name.set("Shabinder Singh")
                            email.set("dev.shabinder@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/Shabinder/youtube-api-dl.git")
                        developerConnection.set("scm:git:ssh://github.com/Shabinder/youtube-api-dl.git")
                        url.set("https://github.com/Shabinder/youtube-api-dl/")
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/Shabinder/youtube-api-dl/issues")
                    }
                }
            }
        }

        val signingKey = "SIGNING_KEY".byProperty
        val signingPwd = "SIGNING_PWD".byProperty
        if (signingKey.isNullOrBlank() || signingPwd.isNullOrBlank()) {
            logger.info("Signing Disable as the PGP key was not found")
        } else {
            logger.warn("Usign $signingKey - $signingPwd")
            signing {
                useInMemoryPgpKeys(signingKey, signingPwd)
                sign(publishing.publications["release"])
            }
        }
    }
}

val String.byProperty: String? get() = gradleLocalProperties(rootDir).getProperty(this)