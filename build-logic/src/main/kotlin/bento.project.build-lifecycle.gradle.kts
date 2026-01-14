/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2023 SAIC. All Rights Reserved.
 ******************************************************************************/

import bento.gradle.lifecycle.BuildLifecycle.ALL_CLASSES_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP

tasks {

    register(ALL_CLASSES_TASK_NAME) {
        description = "Compiles all main and test code for all applicable " +
                "subprojects and test suites."
        group = BUILD_GROUP
    }
}
