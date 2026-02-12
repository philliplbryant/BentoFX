/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")
    id("bento.release.publish-convention")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence API"

dependencies {

    api(projects.core)

    api(libs.javafx.graphics)

    implementation(libs.javafx.base)
    implementation(libs.slf4j.api)

    compileOnly(libs.jetbrains.annotations)
}
