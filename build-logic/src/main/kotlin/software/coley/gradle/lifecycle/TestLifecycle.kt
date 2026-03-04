/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2023 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.gradle.lifecycle

import org.gradle.api.Project
import software.coley.gradle.lifecycle.TestReportMode.*

/**
 * Constants for test lifecycle tasks.
 */
object TestLifecycle {

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
