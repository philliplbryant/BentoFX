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
     * will cause third-party dependencies declared in the BOM to be overriden
     * by those platform declarations.
     */

    api(platform(libs.xmlunit2.bom))

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
        api(libs.xmlunit1)
    }
}
