/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence for JSON"

dependencies {

    api(projects.persistence.api)

    implementation(projects.persistence.codec.common)

    implementation(libs.jackson.databind)

    compileOnly(libs.javafx.controls)
    compileOnly(libs.jetbrains.annotations)
}
