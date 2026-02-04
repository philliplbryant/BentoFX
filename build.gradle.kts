/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel.CURRENT
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL

plugins {
    // Apply the base plugin to add `clean` to the root project.
    id("base")
    id("bento.project.build-lifecycle")

    alias(libs.plugins.jreleaser.gradlePlugin)
    alias(libs.plugins.dependencyAnalysis.gradlePlugin)
    // Declare with `apply false` to include in Versions Gradle Plugin checks.
    alias(libs.plugins.jvmDependencyConflict.detection.gradlePlugin) apply false
    // Declare with `apply false` to include in Versions Gradle Plugin checks.
    alias(libs.plugins.jvmDependencyConflict.resolution.gradlePlugin) apply false
    alias(libs.plugins.versionsCheck.gradlePlugin)
}

dependencyAnalysis {
    issues {
        // Configure for all projects
        all {
            onDuplicateClassWarnings { severity("fail") }
            onIncorrectConfiguration {
                severity("fail")
            }
            onModuleStructure { severity("fail") }
            onUnusedDependencies {
                severity("fail")
                exclude(
                        // Exclude common testing dependencies that we apply to all
                        // projects regardless of use (for the sake of convenience).
                        "junit:junit",
                        "org.assertj:assertj-core",
                        "org.junit.jupiter:junit-jupiter",
                        "org.junit.jupiter:junit-jupiter-api",
                )
            }
            onUnusedAnnotationProcessors { severity("fail") }
            onUsedTransitiveDependencies { severity("fail") }
        }
    }
    usage {
        analysis {
            checkSuperClasses(true)
        }
    }
    useTypesafeProjectAccessors(true)
}

tasks {

    named<DependencyUpdatesTask>("dependencyUpdates").configure {
        checkConstraints = true
        rejectVersionIf {
            isNonStable(candidate.version)
        }
        gradleReleaseChannel = CURRENT.id
    }

    // Use this task to upgrade Gradle in order to keep
    // gradle-wrapper.properties in sync.
    named<Wrapper>("wrapper").configure {
        gradleVersion = "9.3.0"
        // Using `ALL` instead of `BIN` to provide support for developers using
        // the Gradle scripts.
        distributionType = ALL
        networkTimeout = 60000
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any {
        version.uppercase().contains(it)
    }
    val unstableKeyword =
        listOf("""M\d+""").any { version.uppercase().contains(it.toRegex()) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = (stableKeyword && !unstableKeyword) || regex.matches(version)
    return isStable.not()
}
