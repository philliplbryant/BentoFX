package software.coley.bentofx.persistence.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.event.DockEvent;
import software.coley.bentofx.event.DockEventListener;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.BentoStateException;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract {@link LayoutSaver} that automatically saves the docking layout at
 * schedule intervals and when the application exits. To do so efficiently, this
 * class listens for {@link DockEvent}s to track whether changes have been made
 * to the layout and only saves when changes have actually been made. Because
 * this class implements :@link {@link AutoCloseable}, it can be used in a
 * try-with- resources block to automatically call {@link #close()} to save the
 * docking layout when the try block exits.
 */
public abstract class AbstractAutoCloseableLayoutSaver
        implements LayoutSaver, AutoCloseable, DockEventListener {

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractAutoCloseableLayoutSaver.class);

    private static final long DEFAULT_AUTO_SAVE_INTERVAL_IN_MINUTES = 5;

    private static final Cleaner CLEANER = Cleaner.create();

    private final Cleaner.Cleanable cleanable;

    private boolean isAutoSaveEnabled;
    private @Nullable ScheduledExecutorService scheduler;
    private final @NotNull AtomicBoolean wasDockEventReceived =
            new AtomicBoolean(false);

    private long autoSaveInterval =
            DEFAULT_AUTO_SAVE_INTERVAL_IN_MINUTES;

    private TimeUnit autoSaveTimeUnit =
            TimeUnit.MINUTES;

    /**
     * The {@link Bento} whose {@link DockEvent}s will be used to determine
     * whether the docking layout should be saved.
     */
    protected final @NotNull Bento bento;

    /**
     * Constructs an {@code AbstractAutoCloseableLayoutSaver} and listens for
     * {@link DockEvent}s originating from the specified {@link Bento} to
     * determine whether the docking layout should be saved at scheduled
     * intervals and/or when exiting a try-with-resources block.
     *
     * @param bento the {@link Bento} whose {@link DockEvent}s will be used to
     *              determine whether the docking layout should be saved.
     */
    protected AbstractAutoCloseableLayoutSaver(final @NotNull Bento bento) {

        this.bento = Objects.requireNonNull(bento);
        this.cleanable = CLEANER.register(
                this,
                new RunnableResource(this::autoSave)
        );
        enableAutoSave(autoSaveInterval, autoSaveTimeUnit);
    }

    /**
     * Returns {@code true} when auto save is enabled; otherwise, returns
     * {@code false}.
     *
     * @return {@code true} when auto save is enabled; otherwise, returns
     * {@code false}.
     */
    public boolean isAutoSaveEnabled() {
        return isAutoSaveEnabled;
    }

    /**
     * Enables functionality to automatically save the docking layout at the
     * specified interval.
     *
     * @param autoSaveInterval the interval for which automatic saving will be
     *                         scheduled.
     * @param autoSaveTimeUnit the unit of time in which the
     *                         {@code autoSaveInterval} will be scheduled.
     * @see #disableAutoSave() to disable automatic saving.
     */
    public void enableAutoSave(
            final @NotNull Long autoSaveInterval,
            final @NotNull TimeUnit autoSaveTimeUnit
    ) {
        this.isAutoSaveEnabled = true;
        this.autoSaveInterval = autoSaveInterval;
        this.autoSaveTimeUnit = autoSaveTimeUnit;

        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this::autoSave,
                    autoSaveInterval,
                    autoSaveInterval,
                    autoSaveTimeUnit
            );
        }

        bento.events().addEventListener(this);
    }

    /**
     * Disables functionality to automatically save the docking layout.
     *
     * @see #enableAutoSave(Long, TimeUnit)  to enable automatic saving.
     */
    public void disableAutoSave() {

        this.isAutoSaveEnabled = false;

        if (scheduler != null) {
            scheduler.close();
            scheduler = null;
        }

        bento.events().removeEventListener(this);
    }

    @Override
    public void onDockEvent(final @NotNull DockEvent event) {
        logger.debug("Dock event received: {}", event);
        this.wasDockEventReceived.set(true);
    }

    @Override
    public void close() {

        if (isAutoSaveEnabled) {
            cleanable.clean();
        }

        disableAutoSave();
    }

    /**
     * Called when the scheduled interval to automatically save the docking
     * layout expires, this method ensures the layout is only saved when
     * {@link DockEvent}s have occurred since the last save, indicating
     * changes have been made that need to be saved.
     */
    private void autoSave() {
        try {
            if (wasDockEventReceived.getAndSet(false)) {
                logger.debug(
                        "Dock events have been received; " +
                                "attempting to save layout."
                );

                saveLayout();
            } else {

                logger.debug(
                        "No dock events have been received; " +
                                "will not attempt to save layout."
                );
            }

        } catch (BentoStateException e) {
            logger.warn(
                    "Could not auto-save docking layout",
                    e
            );
        }
    }

    /**
     * The {@code RunnableResource} encapsulates the cleaning action.
     * It is implemented as a {@code record} to avoid implicitly holding a
     * reference to the outer {@link AbstractAutoCloseableLayoutSaver} instance.
     */
    private record RunnableResource(@NotNull Runnable runnable)
            implements Runnable {

        private RunnableResource(Runnable runnable) {
            this.runnable = Objects.requireNonNull(runnable);
        }

        @Override
        public void run() {
            logger.debug("Running runnable on close: {}", runnable);
            runnable.run();
        }
    }
}
