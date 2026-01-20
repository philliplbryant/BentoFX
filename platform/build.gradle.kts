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

    // First instead of alphabetically because the Spring Boot BOM includes
    // many third-party dependencies that we may want to override with
    // subsequent platform declarations.
    api(platform(libs.springboot.bom)) {
        because(
            """
            We leverage the Spring Boot BOM as the reference set for all Spring
            and many third-party libraries. Before adding additional BOMs or
            individual artifact version constraints, you can see whether the
            artifact in question is already covered (replacing "current" with
            the `springBootVersion` value as needed):
            https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-dependency-versions.html

            The Spring Boot BOM includes additional non-Spring dependencies, so
            importing as an enforcedPlatform could be problematic.

            Gradle processes platform declarations in the order they appear.
            When multiple platforms declare the same dependency, the last
            declared platform takes precedence.
            """.trimIndent()
        )
        version {
            strictly(libs.versions.springboot.constraint.get())
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
