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
 * using an H2 database.
 */
description = "BentoFX Persistence for H2 Database"

dependencies {

    api(projects.persistence.api)

    api(libs.jakarta.persistence)
}
