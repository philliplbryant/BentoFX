/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {

    id("bento.project.project-convention")
    id("application")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Demo"

application {
    applicationName = description ?: name
    mainClass = "software.coley.boxfx.demo.Runner"
    applicationDefaultJvmArgs += listOf(
        "-Xms256m",
        "-Xmx1024m",
    )
}

// FIXME BENTO-13: The Gradle `run` task fails with "Error: --add-modules
//  requires modules to be specified" (IntelliJ run configuration still works).

dependencies {

    implementation(projects.core)
    implementation(projects.persistence.api)

    implementation(libs.slf4j.api)
    implementation(libs.javafx.base)
    implementation(libs.javafx.controls)
    implementation(libs.javafx.graphics)

    compileOnly(libs.jakarta.persistence)
    compileOnly(libs.jetbrains.annotations)

//    runtimeOnly(projects.persistence.codec.json)
    runtimeOnly(projects.persistence.codec.xml)
//    runtimeOnly(projects.persistence.storage.db.h2)
    runtimeOnly(projects.persistence.storage.file)
    runtimeOnly(libs.slf4j.jdk14)
}

tasks {

    /**
     * A JavaExec task is created by IntelliJ and is called when executing
     * application run configurations.
     */
    withType<JavaExec>().configureEach {

        if (name == "run" || name.endsWith("main()")) {

            notCompatibleWithConfigurationCache(
                "This task relies on Task.extensions, which is only " +
                        "available during the configuration phase."
            )
        }
    }
}
