package software.coley.gradle.lifecycle

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import static software.coley.gradle.lifecycle.TestLifecycle.TestReportMode.ALL
import static software.coley.gradle.lifecycle.TestLifecycle.TestReportMode.CI
import static software.coley.gradle.lifecycle.TestLifecycle.TestReportMode.DEV
import static software.coley.gradle.lifecycle.TestLifecycle.TestReportMode.OFF

class TestLifecycle {
	public static final String CHECK_INTEGRATION_TASK_NAME = 'checkIntegration'
	public static final String CHECK_FUNCTIONAL_TASK_NAME = 'checkFunctional'
	public static final String CHECK_ALL_TASK_NAME = 'checkAll'

	static Provider<Boolean> enableJacoco(ProviderFactory providers) {
		return providers.gradleProperty('collectCoverage')
				.map(it ->
                        it != 'false'
				).orElse(false)
	}

	static Provider<TestReportMode> getTestReportMode(ProviderFactory providers) {
		return providers.gradleProperty('testReportMode')
				.map(it ->
						switch (it.toLowerCase()) {
							case 'off' -> OFF
							case 'all' -> ALL
							case 'ci' -> CI
							default -> DEV
						}
				).orElse(DEV)
	}

	enum TestReportMode {
		OFF,
		ALL,
		CI,
		DEV
	}

	private TestLifecycle() {
		throw new UnsupportedOperationException(
				'Utility classes should not be instantiated.'
		)
	}
}
