package software.coley.gradle.lifecycle

final class TestLifecycle {

	public static final String CHECK_INTEGRATION_TASK_NAME = 'checkIntegration'
	public static final String CHECK_FUNCTIONAL_TASK_NAME = 'checkFunctional'
	public static final String CHECK_ALL_TASK_NAME = 'checkAll'

	private TestLifecycle() {
		throw new UnsupportedOperationException(
				'Utility classes should not be instantiated.'
		)
	}
}
