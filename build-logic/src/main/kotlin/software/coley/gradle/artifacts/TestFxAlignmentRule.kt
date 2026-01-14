/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.gradle.artifacts

import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

/**
 * At present, TestFX does not publish Gradle module metadata or a BOM, so
 * Gradle won't automatically align versions of its dependencies when resolving
 * version conflicts. This rule can be applied to tell Gradle to perform such
 * alignment.
 */
abstract class TestFxAlignmentRule : ComponentMetadataRule {

    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group == "org.testfx"
                // OpenJFX Monocle is versioned separately.
                && id.name != "openjfx-monocle"
            ) {
                belongsTo(
                    "org.testfx:testfx-virtual-bom:${id.version}"
                )
            }
        }
    }
}
