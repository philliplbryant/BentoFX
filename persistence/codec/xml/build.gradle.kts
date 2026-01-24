/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("bento.project.project-convention")

    alias(libs.plugins.javafx.gradlePlugin)
}

description = "BentoFX Persistence for XML"

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
