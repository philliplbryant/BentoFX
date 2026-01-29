/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {

    id("bento.project.project-convention")
    alias(libs.plugins.javafx.gradlePlugin)
}

description = "H2 Database Implementation of the Bento LayoutStorage interface."

dependencies {

    api(projects.persistence.storage.db.common)

    implementation(libs.byte.buddy)
    implementation(libs.hibernate.hikari.cp)
    implementation(libs.hibernate.validator)
    implementation(libs.jakarta.cdi.api)
    implementation(libs.jakarta.el)
    implementation(libs.jakarta.transaction)
    implementation(libs.zaxxer.hikari.cp)

    runtimeOnly(libs.h2)
    runtimeOnly(libs.javafx.controls)
}
