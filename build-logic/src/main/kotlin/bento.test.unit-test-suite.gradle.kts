/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2023 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.gradle.conventions.test.TestSuites.UNIT_TEST
import software.coley.gradle.lifecycle.BuildLifecycle.ALL_CLASSES_TASK_NAME
import software.coley.gradle.lifecycle.TestLifecycle.enableJacoco

plugins {
    id("jacoco")
    id("jvm-test-suite")
    id("bento.project.build-lifecycle")
}

val enableJacoco = enableJacoco(project)

@Suppress("UnstableApiUsage")
testing {
    suites {
        named<JvmTestSuite>(UNIT_TEST) {
            targets {
                all {
                    testTask.configure {
                        maxHeapSize = "4g"
                        maxParallelForks =
                            (Runtime.getRuntime().availableProcessors() / 2)
                                .takeIf { it > 0 } ?: 1
                        jvmArgs(
                            // Open java.base packages to reflection from the
                            // unnamed module as needed by EqualsVerifier.
                            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                            "--add-opens", "java.base/java.util.zip=ALL-UNNAMED",
                        )
                    }
                }
            }
        }
    }
}

tasks {

    val test by existing

    val jacocoTestReport = named<JacocoReport>("jacocoTestReport")

    jacocoTestReport.configure {
        shouldRunAfter(test)
    }

    val testClasses by existing

    named(ALL_CLASSES_TASK_NAME) {
        dependsOn(testClasses)
    }

    check {
        dependsOn(test)
        if (enableJacoco) {
            dependsOn(jacocoTestReport)
        }
    }
}
