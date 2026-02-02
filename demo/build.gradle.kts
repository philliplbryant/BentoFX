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

    // <editor-fold desc="JSON">
//    implementation(projects.persistence.codec.json)
    // </editor-fold>

    // <editor-fold desc="XML">
    implementation(projects.persistence.codec.xml)
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Storage implementation">

    // <editor-fold desc="Database">
//    implementation(projects.persistence.storage.db.common)
//    implementation(projects.persistence.storage.db.h2)
    // </editor-fold>

    // <editor-fold desc="File">
    implementation(projects.persistence.storage.file)
    // </editor-fold>

    // </editor-fold>
}

/**
 * There appears to be a bug in the Dependency Analysis Gradle Plugin (DAGP),
 * which erroneously reports these dependencies to be unused and/or runtime only.
 * Generally speaking, they are runtime only dependencies **except** they are
 * required to compile module-info.java. Exclude them here until the DAGP fixes
 * the bug.
 */
dependencyAnalysis {
    issues {
        onUnusedDependencies {
            exclude(projects.persistence.storage.db.h2)
        }
    }
}

tasks {
    named<JavaExec>("run").configure {
        notCompatibleWithConfigurationCache(
            "This task relies on Task.extensions, which is only " +
                    "available during the configuration phase."
        )
    }
}
