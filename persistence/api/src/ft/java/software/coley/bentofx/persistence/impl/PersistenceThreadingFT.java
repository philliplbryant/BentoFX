package software.coley.bentofx.persistence.impl;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.BentoStateTimeoutException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static software.coley.bentofx.persistence.impl.PersistenceThreading.OFF_FX_EXECUTOR_THREAD_NAME;

@ExtendWith(ApplicationExtension.class)
class PersistenceThreadingFT {

	private static final String RAN_ON_FX_THREAD = "ranOnFxThread";
	private static final String RAN_ON_FX_THREAD_GET_DESCRIPTION = "ranOnFxThread.get()";

	/**
	 * Budget used by the timeout tests. Small enough to keep them fast, large
	 * enough that a loaded continuous integration machine will not trip it
	 * spuriously in the tests that expect success.
	 */
	private static final long SHORT_TIMEOUT_MILLIS = 250L;

	/**
	 * How long to wait for a latch that is expected to be released promptly.
	 * Generous, because exceeding it fails the test rather than slowing it.
	 */
	private static final long LATCH_TIMEOUT_SECONDS = 10L;

	@Test
	void callOnFxThreadRunsImmediatelyWhenAlreadyOnFxThread(FxRobot robot) {
		final AtomicReference<Boolean> ranOnFxThread = new AtomicReference<>();

		robot.interact(() -> {
			try {
				ranOnFxThread.set(
						PersistenceThreading.callOnFxThread(
								Platform::isFxApplicationThread
						)
				);
			} catch (final BentoStateException e) {
				throw new AssertionError(e);
			}
		});

		assertThat(ranOnFxThread.get())
				.describedAs(RAN_ON_FX_THREAD_GET_DESCRIPTION)
				.isTrue();
	}

	@Test
	void callOnFxThreadDispatchesWhenCalledOffFxThread() throws BentoStateException {
		final Boolean ranOnFxThread = PersistenceThreading.callOnFxThread(
				Platform::isFxApplicationThread
		);

		assertThat(ranOnFxThread)
				.describedAs(RAN_ON_FX_THREAD)
				.isTrue();
	}

	@Test
	void callOffFxThreadDispatchesWhenCalledOnFxThread(FxRobot robot) {
		final AtomicReference<Boolean> ranOnFxThread = new AtomicReference<>();

		robot.interact(() -> {
			try {
				ranOnFxThread.set(
						PersistenceThreading.callOffFxThread(
								Platform::isFxApplicationThread
						)
				);
			} catch (final BentoStateException e) {
				throw new AssertionError(e);
			}
		});

		assertThat(ranOnFxThread.get())
				.describedAs(RAN_ON_FX_THREAD_GET_DESCRIPTION)
				.isFalse();
	}

	@Test
	void callOffFxThreadRunsImmediatelyWhenAlreadyOffFxThread()
			throws BentoStateException {
		final AtomicBoolean called = new AtomicBoolean();

		final Boolean ranOnFxThread = PersistenceThreading.callOffFxThread(() -> {
			called.set(true);
			return Platform.isFxApplicationThread();
		});

		assertThat(called)
				.describedAs("called")
				.isTrue();
		assertThat(ranOnFxThread)
				.describedAs(RAN_ON_FX_THREAD)
				.isFalse();
	}

	/**
	 * Successive hand-offs must reuse one shared worker rather than create a
	 * thread per call. Also pins the naming, which is what makes the thread
	 * identifiable in a dump of a frozen UI.
	 */
	@Test
	void callOffFxThreadReusesOneSharedThreadAcrossCalls(FxRobot robot) {

		final AtomicReference<Thread> firstThread = new AtomicReference<>();
		final AtomicReference<Thread> secondThread = new AtomicReference<>();

		robot.interact(() -> {
			try {
				firstThread.set(PersistenceThreading.callOffFxThread(Thread::currentThread));
				secondThread.set(PersistenceThreading.callOffFxThread(Thread::currentThread));
			} catch (final BentoStateException e) {
				throw new AssertionError(e);
			}
		});

		assertThat(firstThread.get())
				.describedAs("thread running the first hand-off")
				.isNotNull()
				.isSameAs(secondThread.get());
		assertThat(firstThread.get().getName())
				.describedAs("worker thread name")
				.isEqualTo(OFF_FX_EXECUTOR_THREAD_NAME);
		assertThat(firstThread.get().isDaemon())
				.describedAs("worker thread is a daemon")
				.isTrue();
	}

	/*
	 * The following tests use unbounded wait on the JavaFX application thread
	 * to hang the caller forever. The production trigger is calling close()
	 * after the JavaFX toolkit has shut down: Platform.runLater then accepts a
	 * task and silently never runs it, and JavaFX exposes no public way to ask
	 * whether the toolkit is alive. These tests cannot shut the toolkit down -
	 * it is not restartable, and this suite shares one JVM, and doing so would
	 * break every test that follows.
	 *
	 * Instead, they reproduce the condition the hang depends on, which is the
	 * part that actually matters: a task accepted by runLater that does not run
	 * within its budget. Occupying the JavaFX thread with a latch produces
	 * exactly that state and is fully reversible.
	 */

	@Test
	void callOnFxThreadTimesOutInsteadOfHangingWhenFxThreadNeverRunsTask()
			throws InterruptedException {
		final CountDownLatch fxThreadOccupied = new CountDownLatch(1);
		final CountDownLatch releaseFxThread = new CountDownLatch(1);

		occupyFxThread(fxThreadOccupied, releaseFxThread);

		try {
			assertThat(fxThreadOccupied.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS))
					.describedAs("JavaFX thread became occupied")
					.isTrue();

			// Before the fix this call never returned.
			assertThatThrownBy(() ->
					PersistenceThreading.callOnFxThread(
							() -> Boolean.TRUE,
							SHORT_TIMEOUT_MILLIS
					)
			)
					.describedAs("callOnFxThread with an occupied JavaFX thread")
					.isInstanceOf(BentoStateTimeoutException.class)
					.hasMessageContaining(String.valueOf(SHORT_TIMEOUT_MILLIS));
		} finally {
			releaseFxThread.countDown();
		}

		drainFxQueue();
	}

	/**
	 * A timed-out task must be canceled, not merely abandoned. It is still
	 * queued on the JavaFX thread at that point, so without the cancel it runs
	 * once the thread frees up and mutates JavaFX state on behalf of a caller
	 * that already gave up and, in the restore case, already fell back to a
	 * different layout.
	 */
	@Test
	void callOnFxThreadCancelsTimedOutTaskSoItNeverRuns()
			throws InterruptedException {

		final CountDownLatch fxThreadOccupied = new CountDownLatch(1);
		final CountDownLatch releaseFxThread = new CountDownLatch(1);
		final AtomicBoolean taskRan = new AtomicBoolean();

		occupyFxThread(fxThreadOccupied, releaseFxThread);

		try {
			assertThat(fxThreadOccupied.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS))
					.describedAs("JavaFX thread became occupied")
					.isTrue();

			assertThatThrownBy(() ->
					PersistenceThreading.callOnFxThread(
							() -> {
								taskRan.set(true);
								return Boolean.TRUE;
							},
							SHORT_TIMEOUT_MILLIS
					)
			).isInstanceOf(BentoStateTimeoutException.class);
		} finally {
			releaseFxThread.countDown();
		}

		// Let the JavaFX thread work through anything still queued, including
		// the abandoned task, before checking that it did not execute.
		drainFxQueue();

		assertThat(taskRan)
				.describedAs("timed-out task ran after being abandoned")
				.isFalse();
	}

	/**
	 * Guards the other direction: the budget exists to detect a task that will
	 * never run, not one that is briefly late. A task whose turn comes up within
	 * the budget must still succeed.
	 */
	@Test
	void callOnFxThreadSucceedsWhenFxThreadIsBusyButFreesUpWithinBudget()
			throws InterruptedException, BentoStateException {
		final CountDownLatch fxThreadOccupied = new CountDownLatch(1);
		final CountDownLatch releaseFxThread = new CountDownLatch(1);

		occupyFxThread(fxThreadOccupied, releaseFxThread);

		assertThat(
				fxThreadOccupied.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
		)
				.describedAs("JavaFX thread became occupied")
				.isTrue();

		// Free the JavaFX thread well inside a generous budget.
		final Thread releaser = new Thread(() -> {
			try {
				Thread.sleep(50L);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			releaseFxThread.countDown();
		}, "fx-thread-releaser");
		releaser.setDaemon(true);
		releaser.start();

		try {
			final Boolean ranOnFxThread = PersistenceThreading.callOnFxThread(
					Platform::isFxApplicationThread,
					TimeUnit.SECONDS.toMillis(LATCH_TIMEOUT_SECONDS)
			);

			assertThat(ranOnFxThread)
					.describedAs(RAN_ON_FX_THREAD)
					.isTrue();
		} finally {
			releaseFxThread.countDown();
			releaser.join(TimeUnit.SECONDS.toMillis(LATCH_TIMEOUT_SECONDS));
		}
	}

	/**
	 * The timeout type must stay assignable to {@link BentoStateException} so
	 * existing callers that catch the general type keep compiling and working,
	 * while callers that need to tell a timeout apart - notably
	 * {@code DockingLayoutRestorer}, which must not substitute a default layout
	 * on timeout - can still catch it specifically.
	 */
	@Test
	void timeoutExceptionRemainsCatchableAsBentoStateException() {
		final BentoStateTimeoutException timeoutException =
				new BentoStateTimeoutException("timed out");

		assertThat(timeoutException)
				.describedAs("BentoStateTimeoutException")
				.isInstanceOf(BentoStateException.class);
	}

	/**
	 * Occupies the JavaFX application thread until {@code releaseFxThread} is
	 * counted down, so that work submitted through
	 * {@link Platform#runLater(Runnable)} is accepted but cannot run.
	 *
	 * @param fxThreadOccupied counted down once the JavaFX thread is actually
	 * blocked, so callers do not race the submission.
	 * @param releaseFxThread awaited on the JavaFX thread; count it down to let
	 * the thread continue. Always count it down in a {@code finally} block:
	 * leaving the JavaFX thread blocked would break every later test.
	 */
	private static void occupyFxThread(
			final CountDownLatch fxThreadOccupied,
			final CountDownLatch releaseFxThread
	) {
		Platform.runLater(() -> {
			fxThreadOccupied.countDown();
			try {
				if (!releaseFxThread.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
					// Safety valve. Without this a failing test could leave the
					// JavaFX thread blocked for the rest of the suite.
					Thread.currentThread().interrupt();
				}
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	/**
	 * Blocks until the JavaFX application thread has worked through everything
	 * currently queued. Because {@link Platform#runLater(Runnable)} preserves
	 * submission order, a fence submitted now completes only after the tasks
	 * submitted before it have been dealt with.
	 */
	private static void drainFxQueue() throws InterruptedException {
		final CountDownLatch drained = new CountDownLatch(1);
		Platform.runLater(drained::countDown);

		if (!drained.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			fail("JavaFX application thread did not drain its queue");
		}
	}
}
