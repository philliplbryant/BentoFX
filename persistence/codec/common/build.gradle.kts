/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")

    alias(libs.plugins.javafx.gradlePlugin)
}

/**
 * This module provides persistence for the BentoFX docking framework.
 */
description = "BentoFX Persistence"

dependencies {

    api(projects.coreFramework)
    api(projects.persistence.api)

    api(libs.jackson.annotations)
    api(libs.jakarta.xmlbinding.api)
    api(libs.javafx.graphics)

    compileOnly(libs.jakarta.annotation)
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.javafx.base)
    // TODO BENTO-13: Fix the DAGP to pass when including
    //  libs.javafx.controls; it is a required implementation dependency.
    implementation(libs.javafx.controls)
    implementation(libs.slf4j.api)

    runtimeOnly(libs.h2)
    runtimeOnly(libs.hibernate.core)
}
