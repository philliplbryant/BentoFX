import org.gradle.api.JavaVersion.VERSION_17
import software.coley.gradle.project.ProjectConstants.JAVA_FX_VERSION
import software.coley.gradle.project.ProjectConstants.JAVA_VERSION

plugins {
    java
    `maven-publish`
    signing

    alias(libs.plugins.javafx.gradlePlugin)
    alias(libs.plugins.jreleaser.gradlePlugin)
    alias(libs.plugins.jvmDownGrader.gradlePlugin)
}

dependencies {
    compileOnly(libs.javafx.controls)
    // TODO BENTO-13: Put in a request to use libs.jetbrains.annotations instead
    //  of libs.jakarta.annotation.
    compileOnly(libs.jakarta.annotation)
}

javafx {
    version = JAVA_FX_VERSION.majorVersion
    modules = listOf("javafx.controls")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(JAVA_VERSION.toString())
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

jvmdg {
    downgradeTo = VERSION_17
}

tasks.javadoc {
    (options as? StandardJavadocDocletOptions)
        ?.addStringOption("Xdoclint:none", "-quiet")
}

// There is currently a bug with the shading portion of 'jvmdowngrader' which
// prevents the 'module-info' class from being copied into the final output.
// These two tasks will generate a modified shaded output jar that includes it.

val copyModuleInfo by tasks.registering(Copy::class) {
    // Ensure the downgrader tasks all run
    dependsOn(tasks.named("shadeDowngradedApi"))

    // Copy the downgraded module-info into a temporary location
    from(zipTree(tasks.named("downgradeJar").get().outputs.files.singleFile)) {
        include("module-info.class")
    }
    into(layout.buildDirectory.dir("module-info-temp"))
}

val mergeShadedJar by tasks.registering(Jar::class) {
    // Run the copy task defined above
    dependsOn(copyModuleInfo)

    archiveClassifier.set("merged-shaded")

    // Merge the shaded output and our module-info class we put in the temp dir
    from(zipTree(tasks.named("shadeDowngradedApi").get().outputs.files.singleFile))
    from(layout.buildDirectory.dir("module-info-temp"))
}

// Ensure builds emit the corrected shaded jar
tasks.assemble {
    dependsOn(mergeShadedJar)
}

// Setup publishing to pull from our slightly modified outputs
val includeTransitives = false

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            // Include our corrected shaded/downgraded jar
            artifact(mergeShadedJar.map { it.outputs.files.singleFile }) {
                builtBy(mergeShadedJar)
            }

            // Include sources + javadoc jars
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))

            pom {
                name.set(project.name)
                description.set("A docking system for JavaFX.")
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
                        id.set("Col-E")
                        name.set("Matt Coley")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Col-E/BentoFX.git")
                    developerConnection.set("scm:git:ssh://github.com/Col-E/BentoFX.git")
                    url.set("https://github.com/Col-E/BentoFX")
                }

                // We aren't using `from(components["java"])` so we need to rebuild the dependencies node ourselves
                if (includeTransitives) {
                    withXml {
                        val allDeps =
                            project.configurations.getByName("compileClasspath")
                                .resolvedConfiguration
                                .firstLevelModuleDependencies

                        val root = asNode()
                        val depNode = root.appendNode("dependencies")
                        allDeps.forEach { d ->
                            val dn = depNode.appendNode("dependency")
                            dn.appendNode("groupId", d.moduleGroup)
                            dn.appendNode("artifactId", d.name)
                            dn.appendNode("version", d.moduleVersion)
                        }
                    }
                }
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    signing {
        pgp {
            setActive("RELEASE")
            armored = true
        }
    }
    release {
        // TODO: This doesn't auto-publish github releases and the 'distribution' block also isn't a viable alternative
        //  Need to look into why it doesn't work. Probably related to the project's "alternative" artifact model...
        github {
            tagName = project.version.toString()
            changelog {
                setFormatted("ALWAYS")
                preset = "conventional-commits"
                contributors {
                    format =
                        "- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}"
                }
            }
        }
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive("RELEASE")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    applyMavenCentralRules = true
                    stagingRepository(
                        layout.buildDirectory.dir("staging-deploy")
                            .get().asFile.path
                    )
                }
            }
        }
    }
}
