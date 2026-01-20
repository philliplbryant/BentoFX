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
 * files.
 */
description = "BentoFX Persistence for Files"

javafx {
    modules(
        "javafx.base",
        "javafx.graphics",
        "javafx.controls",
    )
}

dependencies {

    api(projects.persistence.api)
}
