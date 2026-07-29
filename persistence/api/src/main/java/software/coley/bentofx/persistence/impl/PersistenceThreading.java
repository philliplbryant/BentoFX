package software.coley.bentofx.persistence.impl;

import javafx.application.Platform;
import software.coley.bentofx.persistence.api.BentoStateException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Threading utility methods used by persistence implementations to keep
 * JavaFX object access on the JavaFX application thread while allowing codec
 * and storage work to run off that thread.
 *
 * @author Phil Bryant
 */
final class PersistenceThreading {

    private PersistenceThreading() {
        // Utility class.
    }

    /**
     * Runs the supplied task on the JavaFX application thread and waits for the
     * result. If the current thread is already the JavaFX application thread,
     * the task is executed immediately.
     *
     * @param task task to execute.
     * @param <T> task result type.
     * @return task result.
     * @throws BentoStateException when the task fails or the waiting thread is
     * interrupted.
     */
    static <T> T callOnFxThread(final Callable<T> task)
            throws BentoStateException {
        if (Platform.isFxApplicationThread()) {
            return call(task);
        }

        final FutureTask<T> futureTask = new FutureTask<>(task);
        Platform.runLater(futureTask);
        return get(futureTask);
    }

    /**
     * Runs the supplied task away from the JavaFX application thread and waits
     * for the result. If the current thread is already not the JavaFX
     * application thread, the task is executed immediately.
     *
     * @param task task to execute.
     * @param <T> task result type.
     * @return task result.
     * @throws BentoStateException when the task fails or the waiting thread is
     * interrupted.
     */
    static <T> T callOffFxThread(final Callable<T> task)
            throws BentoStateException {
        if (!Platform.isFxApplicationThread()) {
            return call(task);
        }

        try (final ExecutorService executorService = newSingleThreadExecutor()) {
            return get(executorService.submit(task));
        }
    }

    private static <T> T call(final Callable<T> task)
            throws BentoStateException {
        try {
            return task.call();
        } catch (final BentoStateException e) {
            throw e;
        } catch (final Exception e) {
            throw new BentoStateException(
                    "Persistence task failed",
                    e
            );
        }
    }

    private static <T> T get(final java.util.concurrent.Future<T> future)
            throws BentoStateException {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BentoStateException(
                    "Interrupted while waiting for persistence task",
                    e
            );
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof final BentoStateException bentoStateException) {
                throw bentoStateException;
            }
            throw new BentoStateException(
                    "Persistence task failed",
                    e
            );
        }
    }
}
