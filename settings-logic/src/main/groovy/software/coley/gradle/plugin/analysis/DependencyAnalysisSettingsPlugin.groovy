package software.coley.gradle.plugin.analysis

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Applies and centralizes settings-level dependency-analysis configuration.
 */
@CompileStatic
class DependencyAnalysisSettingsPlugin implements Plugin<Settings> {

    @Override
    void apply(final Settings settings) {
        settings.pluginManager.apply('com.autonomousapps.build-health')
    }
}
