/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")
    id("bento.release.publish-convention")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence"

dependencies {

    api(projects.core)
    api(projects.persistence.api)

    api(libs.jackson.annotations)
    api(libs.jakarta.xmlbinding.api)
    api(libs.javafx.graphics)

    implementation(libs.slf4j.api)
    implementation(libs.javafx.base)
    implementation(libs.javafx.controls)

    compileOnly(libs.jakarta.annotation)
    compileOnly(libs.jetbrains.annotations)
}
