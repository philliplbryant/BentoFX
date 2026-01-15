/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2023 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.gradle.lifecycle

import software.coley.gradle.lifecycle.TestReportMode.ALL
import software.coley.gradle.lifecycle.TestReportMode.CI
import software.coley.gradle.lifecycle.TestReportMode.DEV
import software.coley.gradle.lifecycle.TestReportMode.OFF
import org.gradle.api.Project

/**
 * Constants for test lifecycle tasks.
 */
object TestLifecycle {

    const val CHECK_LEGACY_TASK_NAME = "checkLegacy"
    const val CHECK_INTEGRATION_TASK_NAME = "checkIntegration"
    const val CHECK_FUNCTIONAL_TASK_NAME = "checkFunctional"
    const val CHECK_LOAD_TASK_NAME = "checkLoad"
    const val CHECK_ALL_TASK_NAME = "checkAll"

    fun enableJacoco(project: Project): Boolean {
        val collectCoverage = project.findProperty("collectCoverage") as String?
        return collectCoverage != null && collectCoverage != "false"
    }

    fun getTestReportMode(project: Project): TestReportMode {

        val createTestReports =
            project.findProperty("testReportMode") as String?

        return when (createTestReports?.lowercase()) {
            "off" -> OFF
            "all" -> ALL
            "ci" -> CI
            else -> DEV
        }
    }
}

enum class TestReportMode {
    OFF,
    ALL,
    CI,
    DEV,
}
