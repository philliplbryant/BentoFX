/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")
    id("bento.release.publish-convention")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "H2 Database Implementation of the Bento LayoutStorage interface."

dependencies {

    api(projects.persistence.api)

    api(libs.jakarta.persistence)

    implementation(libs.byte.buddy)
    implementation(libs.hibernate.hikari.cp)
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.cdi.api)
    implementation(libs.jakarta.el)
    implementation(libs.jakarta.transaction)
    implementation(libs.slf4j.api)
    implementation(libs.zaxxer.hikari.cp)

    compileOnly(libs.hibernate.core)

    compileOnly(libs.jetbrains.annotations)

    runtimeOnly(libs.h2)
    runtimeOnly(libs.javafx.controls)
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
            exclude(libs.byte.buddy)
            exclude(libs.jakarta.cdi.api)
            exclude(libs.jakarta.el)
            exclude(libs.jakarta.transaction)
            exclude(libs.zaxxer.hikari.cp)
        }

        onRuntimeOnly {
            exclude(libs.hibernate.hikari.cp)
            exclude(libs.hibernate.validator)
        }
    }
}
