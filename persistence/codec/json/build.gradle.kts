/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2016 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")

    alias(libs.plugins.javafx.gradlePlugin)
}

/**
 * This module implements the Application Programming Interface (API) for
 * encoding and decoding the layout of BentoFX docking framework components
 * using Java Script Object Notation (JSON).
 */
description = "BentoFX Persistence for JSON"

dependencies {

    api(projects.persistence.api)

    compileOnly(libs.jetbrains.annotations)

    implementation(projects.persistence.codec.common)

    implementation(libs.jackson.databind)
}
