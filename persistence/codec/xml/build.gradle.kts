/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")

    alias(libs.plugins.javafx.gradlePlugin)
}

/**
 * This module implements the Application Programming Interface (API) for
 * encoding and decoding the layout of BentoFX docking framework components
 * using the eXtensible Markup Language (XML).
 */
description = "BentoFX Persistence for JSON"

javafx {
    modules(
        "javafx.controls",
    )
}

dependencies {

    api(projects.persistence.api)

    compileOnly(libs.jetbrains.annotations)

    implementation(projects.persistence.codec.common)

    implementation(libs.jakarta.xmlbinding.api)

    runtimeOnly(libs.jakarta.xmlbinding.impl)

}
