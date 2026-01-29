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

dependencies {

    implementation(projects.core)
    implementation(projects.persistence.api)

    implementation(libs.slf4j.api)
    implementation(libs.javafx.base)
    implementation(libs.javafx.controls)
    implementation(libs.javafx.graphics)

    compileOnly(projects.persistence.codec.common)

    // TODO BENTO-13: Put in a request to use libs.jetbrains.annotations instead
    //  of libs.jakarta.annotation.
    compileOnly(libs.jakarta.annotation)
    compileOnly(libs.jakarta.persistence)
    compileOnly(libs.jetbrains.annotations)

    runtimeOnly(libs.slf4j.jdk14)

    // <editor-fold desc="Codec implementation">
//    implementation(projects.persistence.codec.json)
    implementation(projects.persistence.codec.xml)
    // </editor-fold>

    // <editor-fold desc="Storage implementation">
    implementation(projects.persistence.storage.file)
//    implementation(projects.persistence.storage.db.h2)
    // </editor-fold>
}

tasks {
    named<JavaExec>("run").configure {
        notCompatibleWithConfigurationCache(
            "This task relies on Task.extensions, which is only " +
                    "available during the configuration phase."
        )
    }
}
