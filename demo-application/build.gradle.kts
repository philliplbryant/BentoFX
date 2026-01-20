/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {

    kotlin("jvm")
    id("bento.project.project-convention")
    id("application")

    alias(libs.plugins.javafx.gradlePlugin)

    kotlin("plugin.spring")
}

/**
 * This module is a very basic JavaFX application.
 */
description = "BentoFX Demo"

application {
    applicationName = description ?: name
    mainClass.set("com.jregw.demo.BentoFxDemoApplication")
    applicationDefaultJvmArgs += listOf(
        "-Xms256m",
        "-Xmx1024m",
    )
}

javafx {
    version = "19"
// TODO BENTO-13: Create and use a common declaration for the JavaFX version
//    version = libs.versions.javafx.get()
    modules = listOf(
        "javafx.base",
        "javafx.controls",
        "javafx.graphics",
    )
}

dependencies {

    // TODO BENTO-13: Put in a request to use libs.jetbrains.annotations instead
    //  of libs.jakarta.annotation.
    compileOnly(libs.jakarta.annotation)
    compileOnly(libs.jetbrains.annotations)


    implementation(projects.coreFramework)
    implementation(projects.persistence.api)
    implementation(projects.persistence.codec.common)
    implementation(projects.persistence.codec.json)
    implementation(projects.persistence.codec.xml)
    implementation(projects.persistence.storage.database)
    implementation(projects.persistence.storage.file)

    implementation(libs.jakarta.inject)
    implementation(libs.jakarta.persistence)
    implementation(libs.javafx.base)
    implementation(libs.javafx.controls)
    implementation(libs.javafx.graphics)
    implementation(libs.slf4j.api)
    implementation(libs.spring.beans)
    implementation(libs.spring.core)
    implementation(libs.spring.context)
    implementation(libs.springboot)
    implementation(libs.springboot.autoconfigure)

    runtimeOnly(libs.slf4j.jdk14)
    runtimeOnly(libs.springboot.starter) {
        // Exclude SpringBoot Starter Logging to hide Spring log statements
        exclude(
            group = libs.springboot.starter.logging.get().group,
            module = libs.springboot.starter.logging.get().module.name
        )
    }
}
