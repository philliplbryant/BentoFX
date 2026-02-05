/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")
    `maven-publish`
    signing
    id("org.jreleaser")
}

// TODO BENTO-13: Enable JAR signing

// TODO BENTO-13: Revert publishing information to what was originally
//  specified by Col-E

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
                url.set("https://github.com/philliplbryant/BentoFX")
                inceptionYear.set("2025")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id.set("philliplbryant")
                        name.set("Phil Bryant")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/philliplbryant/BentoFX.git")
                    developerConnection.set("scm:git:ssh://github.com/philliplbryant-E/BentoFX.git")
                    url.set("https://github.com/philliplbryant/BentoFX")
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

//jreleaser {
//    signing {
//        pgp {
//            setActive("RELEASE")
//            armored = true
//        }
//    }
//    release {
//        // TODO: This doesn't auto-publish github releases and the
//        //  'distribution' block also isn't a viable alternative. Need to look
//        //  into why it doesn't work. Probably related to the project's
//        //  "alternative" artifact model...
//        github {
//            tagName = project.version.toString()
//            changelog {
//                setFormatted("ALWAYS")
//                preset = "conventional-commits"
//                contributors {
//                    format =
//                        "- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}"
//                }
//            }
//        }
//    }
//    deploy {
//        maven {
//            mavenCentral {
//                create("sonatype") {
//                    setActive("RELEASE")
//                    url = "https://central.sonatype.com/api/v1/publisher"
//                    applyMavenCentralRules = true
//                    stagingRepository(
//                        layout.buildDirectory.dir("staging-deploy")
//                            .get().asFile.path
//                    )
//                }
//            }
//        }
//    }
//}
