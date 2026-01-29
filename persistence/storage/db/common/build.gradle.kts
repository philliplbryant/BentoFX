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

    implementation(libs.slf4j.api)

    compileOnly(libs.hibernate.core)

    compileOnly(libs.jetbrains.annotations)
}
