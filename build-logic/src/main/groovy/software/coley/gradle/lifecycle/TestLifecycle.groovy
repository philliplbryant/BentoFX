package software.coley.gradle.lifecycle

import org.gradle.api.Project

@SuppressWarnings("unused")
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
        return project.providers
                .gradleProperty(COLLECT_COVERAGE_PROPERTY_NAME)
                .map(Boolean.&parseBoolean)
                .orElse(false)
                .get()
    }

    static TestReportMode getTestReportMode(final Project project) {
        return project.providers
                .gradleProperty(TEST_REPORT_MODE_PROPERTY_NAME)
                .map(TestReportMode.&parse)
                .orElse(TestReportMode.DEV)
                .get()
    }

    enum TestReportMode {
        OFF,
        ALL,
        CI,
        DEV

        static TestReportMode parse(final String value) {
            switch (value?.toLowerCase(Locale.ROOT)) {
                case 'off':
                    return OFF
                case 'dev':
                    return DEV
                case 'ci':
                    return CI
                case 'all':
                    return ALL
                default:
                    return DEV
            }
        }
    }
}
