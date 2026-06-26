package software.coley.gradle.lifecycle

import org.gradle.api.Project

final class TestLifecycle {
    public static final String CHECK_INTEGRATION_TASK_NAME = 'checkIntegration'
    public static final String CHECK_FUNCTIONAL_TASK_NAME = 'checkFunctional'
    public static final String CHECK_ALL_TASK_NAME = 'checkAll'

    private static final String COLLECT_COVERAGE_PROPERTY_NAME = 'collectCoverage'
    private static final String TEST_REPORT_MODE_PROPERTY_NAME = 'testReportMode'

    private TestLifecycle() {
        throw new UnsupportedOperationException(
                'Utility classes should not be instantiated.'
        )
    }

    static boolean enableJacoco(final Project project) {
        final Object collectCoverage = project.findProperty(COLLECT_COVERAGE_PROPERTY_NAME)
        return collectCoverage != null && collectCoverage.toString() != 'false'
    }

    static TestReportMode getTestReportMode(final Project project) {
        final Object testReportMode = project.findProperty(TEST_REPORT_MODE_PROPERTY_NAME)
        switch (testReportMode?.toString()?.toLowerCase(Locale.ROOT)) {
            case 'off':
                return TestReportMode.OFF
            case 'all':
                return TestReportMode.ALL
            case 'ci':
                return TestReportMode.CI
            default:
                return TestReportMode.DEV
        }
    }

    enum TestReportMode {
        OFF,
        ALL,
        CI,
        DEV
    }
}
