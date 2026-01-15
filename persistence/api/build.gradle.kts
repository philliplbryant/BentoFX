/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2016 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")

    alias(libs.plugins.javafx.gradlePlugin)
}

/**
 * This module provides the persistence Application Programming Interface (API)
 * for the BentoFX docking framework.
 */
description = "BentoFX Persistence API"

dependencies {

    api(projects.coreFramework)
    api(libs.javafx.graphics)

    compileOnly(libs.jakarta.annotation)
    compileOnly(libs.jetbrains.annotations)

    runtimeOnly(libs.h2)
    runtimeOnly(libs.hibernate.core)
    runtimeOnly(libs.javafx.controls)
}
