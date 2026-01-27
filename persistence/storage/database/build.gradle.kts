/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")

    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence Databases"

dependencies {

    api(projects.persistence.api)

    api(libs.jakarta.persistence)

    implementation(libs.javafx.controls)
    implementation(libs.jakarta.el)     // TODO BENTO-13 is this needed?
    implementation(libs.jakarta.persistence)
    implementation(libs.hibernate.core)

    runtimeOnly(libs.jboss.logging)
}
