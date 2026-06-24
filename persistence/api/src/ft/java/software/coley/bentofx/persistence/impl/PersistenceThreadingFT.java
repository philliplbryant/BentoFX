package software.coley.bentofx.persistence.impl;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.persistence.api.BentoStateException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class PersistenceThreadingFT {

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

        assertThat(ranOnFxThread.get()).isTrue();
    }

    @Test
    void callOnFxThreadDispatchesWhenCalledOffFxThread() throws BentoStateException {
        final Boolean ranOnFxThread = PersistenceThreading.callOnFxThread(
                Platform::isFxApplicationThread
        );

        assertThat(ranOnFxThread).isTrue();
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

        assertThat(ranOnFxThread.get()).isFalse();
    }

    @Test
    void callOffFxThreadRunsImmediatelyWhenAlreadyOffFxThread()
            throws BentoStateException {
        final AtomicBoolean called = new AtomicBoolean();

        final Boolean ranOnFxThread = PersistenceThreading.callOffFxThread(() -> {
            called.set(true);
            return Platform.isFxApplicationThread();
        });

        assertThat(called).isTrue();
        assertThat(ranOnFxThread).isFalse();
    }
}
