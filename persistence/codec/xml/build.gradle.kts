/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")
    id("bento.release.publish-convention")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence for XML"

dependencies {

    api(projects.persistence.api)

    implementation(projects.persistence.codec.common)

    implementation(libs.jakarta.xmlbinding.api)

    compileOnly(libs.javafx.controls)
    compileOnly(libs.jetbrains.annotations)

    runtimeOnly(libs.jakarta.xmlbinding.impl)
}
