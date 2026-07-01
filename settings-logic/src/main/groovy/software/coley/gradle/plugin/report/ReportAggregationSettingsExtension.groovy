package software.coley.gradle.plugin.report

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty

import javax.inject.Inject

/**
 * Configuration extension for the path of all projects used for report
 * aggregation.
 */
@CompileStatic
abstract class ReportAggregationSettingsExtension {
    final ListProperty<String> projectPaths

    @Inject
    ReportAggregationSettingsExtension(final ObjectFactory objects) {
        projectPaths = objects.listProperty(String)
    }
}
