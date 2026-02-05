/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2019 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    // Not using `id("kotlin-dsl")` syntax per
    // https://github.com/gradle/gradle/issues/23884.
    `kotlin-dsl`
}

dependencies {

    implementation(gradleApi())

    implementation(libs.dependencyAnalysis.gradlePlugin.dependency) {
        because(
            "Declared here even though it's not used otherwise in buildSrc to work around " +
                    "https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/issues/667"
        )
    }

    implementation(libs.jvmDependencyConflict.gradlePlugin.dependency)
    implementation(libs.jreleaser.gradlePlugin.dependency)
}
