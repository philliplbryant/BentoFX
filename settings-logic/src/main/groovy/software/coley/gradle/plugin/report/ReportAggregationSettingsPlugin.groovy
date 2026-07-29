package software.coley.gradle.plugin.report

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

/**
 * Project isolation-safe plugin to determine paths for the projects used for
 * report aggregation.
 */
@SuppressWarnings("unused")
@CompileStatic
class ReportAggregationSettingsPlugin implements Plugin<Settings> {

    @Override
    void apply(final Settings settings) {
        final ReportAggregationSettingsExtension extension = settings.gradle.extensions.create(
                'reportAggregationProjects',
                ReportAggregationSettingsExtension
        )

        settings.gradle.settingsEvaluated(new Action<Settings>() {
            @Override
            void execute(final Settings evaluatedSettings) {
                final List<String> aggregatedPaths = evaluatedSettings.rootProject.children
                        .collectMany { ProjectDescriptor descriptor -> walk(descriptor) }
                        .findAll { ProjectDescriptor descriptor ->
                            descriptor.path != ':report-aggregation' &&
                                    !descriptor.path.startsWith(':demos') &&
                                    !descriptor.path.endsWith(':test-fixtures') &&
                                    descriptor.children.empty &&
                                    hasMainJvmSources(descriptor.projectDir)
                        }
                        .collect { ProjectDescriptor descriptor -> descriptor.path }
                        .sort()

                extension.projectPaths.set(aggregatedPaths)
            }
        })
    }

    private static List<ProjectDescriptor> walk(final ProjectDescriptor descriptor) {
        final List<ProjectDescriptor> descriptors = [descriptor]
        descriptor.children.each { ProjectDescriptor child ->
            descriptors.addAll(walk(child))
        }
        return descriptors
    }

    private static boolean hasMainJvmSources(final File projectDir) {
        return containsFiles(new File(projectDir, 'src/main/kotlin'), 'kt') ||
                containsFiles(new File(projectDir, 'src/main/java'), 'java')
    }

    private static boolean containsFiles(final File dir, final String extension) {
        if (!dir.directory) {
            return false
        }

        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            return paths.anyMatch { Path path ->
                Files.isRegularFile(path) && path.fileName.toString().endsWith(".${extension}".toString())
            }
        }
    }
}
