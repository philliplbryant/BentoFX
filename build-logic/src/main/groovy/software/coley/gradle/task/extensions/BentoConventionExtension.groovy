package software.coley.gradle.task.extensions

import org.gradle.api.provider.Provider

import javax.inject.Inject

abstract class BentoConventionExtension {
    final Provider<String> normalizedProjectName

    @Inject
    BentoConventionExtension(final Provider<String> normalizedProjectName) {
        this.normalizedProjectName = normalizedProjectName
    }
}
