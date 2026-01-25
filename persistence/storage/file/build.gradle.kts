/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")

    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence for Files"

dependencies {

    api(projects.persistence.api)

    implementation(libs.javafx.controls)
}
