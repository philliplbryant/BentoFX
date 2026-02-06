/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2019 SAIC. All Rights Reserved.
 ******************************************************************************/

plugins {
    id("java-platform")
}

// See also Gradle Version Catalog at '<root>\gradle\libs.versions.toml'.

javaPlatform {
    allowDependencies()
}

dependencies {

    /*
     * BOMs
     * Ordering matters! Declaring a BOM before subsequent platform declarations
     * will cause third-party dependencies declared in the BOM to be overridden
     * by those platform declarations.
     */

    // First instead of alphabetically because the BOM includes many third-party
    // dependencies that we may want to override with subsequent platform
    // declarations.
    api(platform(libs.jackson.bom)) {
        version {
            strictly(libs.versions.jackson.get())
        }
    }

    constraints {

        /*
         * Bundles
         */

        // See JavaFXComponentMetadataRule for version alignment.
        api(libs.bundles.javafx)

        // See TestFxAlignmentRule.
        api(libs.bundles.testfx)

        /*
         * Individual Dependencies
         */

        api(libs.controlsfx)
        api(libs.jetbrains.annotations)
    }
}
