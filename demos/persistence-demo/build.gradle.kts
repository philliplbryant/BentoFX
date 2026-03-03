/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {

    id("bento.project.project-convention")
    id("application")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence Demo"

application {
    applicationName = description ?: name
    mainModule = "bento.fx.demo.persistence"
    mainClass = "software.coley.boxfx.demo.persistence.Runner"
    applicationDefaultJvmArgs += listOf(
        "-Xms256m",
        "-Xmx1024m",
    )
}

javafx {
    modules = listOf(
        "javafx.controls",
    )
}

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
