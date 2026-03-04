/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            artifactId = project.path
                // Delete the leading ':'
                .substring(1)
                // Replace the remaining ':' with '-'
                .replace(':', '-')

            from(components["java"])

            pom {
                url.set("https://github.com/Col-E/BentoFX")
                inceptionYear.set("2025")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id = "Col-E"
                        name = "Matt Coley"
                    }
                    developer {
                        id.set("philliplbryant")
                        name.set("Phil Bryant")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Col-E/BentoFX.git")
                    developerConnection.set("scm:git:ssh://github.com/Col-E/BentoFX.git")
                    url.set("https://github.com/Col-E/BentoFX")
                }
            }
        }
    }

    repositories {

        val nexusUsername: String =
            findProperty("nexusUsername")?.toString() ?: System.getenv("NEXUS_USERNAME") ?: ""

        val nexusPassword: String =
            findProperty("nexusPassword")?.toString() ?: System.getenv("NEXUS_PASSWORD") ?: ""

        maven {
            url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                uri("https://nexus.jre.saic.com/repository/jre-central-snapshots/")
            } else {
                uri("https://nexus.jre.saic.com/repository/jre-central/")
            }

            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }
}
