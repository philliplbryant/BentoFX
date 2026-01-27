/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.gradle.project.ProjectConstants.JAVA_FX_VERSION

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

javafx {
    version = JAVA_FX_VERSION.majorVersion
    modules = listOf(
        "javafx.base",
        "javafx.controls",
        "javafx.graphics",
    )
}

dependencies {

    compileOnly(projects.persistence.codec.common)

    // TODO BENTO-13: Put in a request to use libs.jetbrains.annotations instead
    //  of libs.jakarta.annotation.
    compileOnly(libs.jakarta.annotation)
    compileOnly(libs.jakarta.inject)
    compileOnly(libs.jakarta.persistence)

    implementation(projects.coreFramework)
    implementation(projects.persistence.api)

    implementation(libs.javafx.base)
    implementation(libs.javafx.controls)
    implementation(libs.javafx.graphics)
    implementation(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

// Specify the codec

//    // JSON
//    implementation(projects.persistence.codec.json)

    // XML
    implementation(projects.persistence.codec.xml)

// Specify the storage

//    // File
//    implementation(projects.persistence.storage.file)

    // Database (H2 / Hibernate / Hikari)
    implementation(projects.persistence.storage.database)
    implementation(libs.byte.buddy)
    implementation(libs.hibernate.hikari.cp)
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.cdi.api)
    implementation(libs.jakarta.el)
    implementation(libs.jakarta.transaction)
    implementation(libs.jboss.logging)
    implementation(libs.zaxxer.hikari.cp)

    runtimeOnly(libs.slf4j.jdk14)
}

tasks {
    named<JavaExec>("run").configure {
        notCompatibleWithConfigurationCache(
            "This task relies on Task.extensions, which is only " +
                    "available during the configuration phase."
        )
    }
}
