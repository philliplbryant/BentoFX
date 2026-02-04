plugins {
    id("bento.project.project-convention")
    id("bento.release.publish-convention")
    alias(libs.plugins.javafx.gradlePlugin)
}

dependencies {

    compileOnlyApi(libs.javafx.controls)

    // TODO BENTO-13: Put in a request to use libs.jetbrains.annotations instead
    //  of libs.jakarta.annotation.
    compileOnly(libs.jakarta.annotation)
}
