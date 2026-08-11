package software.coley.bentofx.persistence.impl;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.event.DockEvent;
import software.coley.bentofx.event.DockEventListener;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.provider.BentoProvider;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract {@link LayoutSaver} that automatically saves the docking layout at
 * scheduled intervals and can be called when the application exits. To do so
 * efficiently, this class listens for {@link DockEvent}s to track whether changes have been made
 * to the layout and only saves when changes have actually been made. Because
 * this class implements {@link AutoCloseable}, it can be used in a
 * try-with-resources block to automatically call {@link #close()} to save the
 * docking layout when the try block exits.
 *
 * <p><b>Auto-save must be started explicitly</b>, by calling
 * {@link #enableAutoSave(Long, TimeUnit)} once the instance is fully
 * constructed. It is deliberately not started by the constructor: doing so
 * handed {@code this} to a scheduler thread and registered it on every
 * {@link Bento}'s event bus before subclass fields had been assigned, so a save
 * firing in that window could observe a half-built object. Subclasses cannot fix
 * that themselves - the superclass constructor always runs first. Instances
 * obtained from
 * {@link software.coley.bentofx.persistence.api.provider.DockingLayoutPersistenceProvider}
 * already have auto-save running.</p>
 *
 * @author Phil Bryant
 */
public abstract class AbstractAutoCloseableLayoutSaver
        implements LayoutSaver, DockEventListener {

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractAutoCloseableLayoutSaver.class);

    private static final long DEFAULT_AUTO_SAVE_INTERVAL_IN_MINUTES = 5;

    private final AtomicBoolean wasDockEventReceived =
            new AtomicBoolean(false);
    private final Set<Bento> listenerBentos = new HashSet<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    private boolean isAutoSaveEnabled;
    private @Nullable ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> scheduledSaveTask;

	/**
     * The {@link BentoProvider} containing the {@link Bento} instances whose
     * {@link DockEvent}s will be used to determine whether the docking layout
     * should be saved.
     */
    protected final BentoProvider bentoProvider;


    /**
     * Constructs an {@code AbstractAutoCloseableLayoutSaver}.
     *
     * <p>Auto-save is <b>not</b> started here - see the class documentation for
     * why. Call {@link #enableAutoSave(Long, TimeUnit)} once construction is
     * complete, or use {@link #startAutoSave(AbstractAutoCloseableLayoutSaver)}
     * which does that for you.</p>
     *
     * @param bentoProvider The {@link BentoProvider} containing the
     *                      {@link Bento} instances whose {@link DockEvent}s
     *                      will be used to determine whether the docking layout
     *                      should be saved.
     */
    protected AbstractAutoCloseableLayoutSaver(
            final BentoProvider bentoProvider
    ) {

        this.bentoProvider = Objects.requireNonNull(bentoProvider);
    }

    /**
     * Starts auto-save on a fully constructed saver at the default interval and
     * returns it, so a factory can build and arm an instance in one expression.
     *
     * <p>This is the safe counterpart to enabling auto-save from the constructor:
     * by the time this runs, every constructor in the hierarchy has completed, so
     * the scheduler thread and the {@link Bento} event buses only ever see a
     * fully initialised object.</p>
     *
     * @param saver the saver to start auto-saving.
     * @param <T> the saver type, preserved so callers do not have to cast.
     * @return the supplied saver, with auto-save running.
     */
    public static <T extends AbstractAutoCloseableLayoutSaver> T startAutoSave(
            final T saver
    ) {
        Objects.requireNonNull(saver);
        saver.enableAutoSave(
                DEFAULT_AUTO_SAVE_INTERVAL_IN_MINUTES,
                TimeUnit.MINUTES
        );
        return saver;
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
            final Long autoSaveInterval,
            final TimeUnit autoSaveTimeUnit
    ) {
        if (closed.get()) {
            throw new IllegalStateException("Cannot enable auto-save after saver has been closed");
        }

        final Long requestedAutoSaveInterval =
                Objects.requireNonNull(autoSaveInterval);
        if (requestedAutoSaveInterval <= 0) {
            throw new IllegalArgumentException("autoSaveInterval must be greater than zero");
        }

        disableAutoSave();

        this.isAutoSaveEnabled = true;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new AutoSaveThreadFactory()
        );
        scheduledSaveTask = this.scheduler.scheduleAtFixedRate(
                () -> autoSave(false),
                autoSaveInterval,
                autoSaveInterval,
                autoSaveTimeUnit
        );
        addListeners();
    }

    /**
     * Disables functionality to automatically save the docking layout.
     *
     * @see #enableAutoSave(Long, TimeUnit)  to enable automatic saving.
     */
    public void disableAutoSave() {

        this.isAutoSaveEnabled = false;

        final ScheduledFuture<?> currentScheduledSaveTask = scheduledSaveTask;
        scheduledSaveTask = null;

        if (currentScheduledSaveTask != null) {
            currentScheduledSaveTask.cancel(false);
        }

        final ScheduledExecutorService currentScheduler = scheduler;
        scheduler = null;

        if (currentScheduler != null) {
            currentScheduler.shutdownNow();
        }

        removeListeners();
    }

    @Override
    public void onDockEvent(final DockEvent event) {
        logger.trace("Dock event received: {}", event);
        this.wasDockEventReceived.set(true);
    }

    @Override
    public void close() {

        if (!closed.compareAndSet(false, true)) {
            return;
        }

        try {
            // Deliberately not gated on isAutoSaveEnabled. Saving on close and
            // saving on a timer are independent concerns, and the class
            // documentation promises try-with-resources will save on exit. Gating
            // meant disableAutoSave() silently also disabled save-on-exit, and
            // now that the constructor no longer starts auto-save it would also
            // mean a directly constructed saver never flushed at all. autoSave
            // still short-circuits when no dock events have been received, so a
            // close with nothing to save remains cheap.
            autoSave(true);
        } finally {
            disableAutoSave();
        }
    }

    /**
     * Saves the layout while the application is shutting down.
     *
     * <p>Separate from {@link #saveLayout()} so an implementation can apply a
     * tighter budget to any wait it performs on another thread. The thread
     * calling {@link #close()} may not be a daemon, so a long wait here can
     * delay or block application exit. The default implementation simply
     * delegates to {@link #saveLayout()}.</p>
     *
     * @throws BentoStateException when the layout cannot be saved.
     */
    protected void saveLayoutForShutdown() throws BentoStateException {
        saveLayout();
    }

    /**
     * Called when the scheduled interval to automatically save the docking
     * layout expires, this method ensures the layout is only saved when
     * {@link DockEvent}s have occurred since the last save, indicating
     * changes have been made that need to be saved.
     *
     * @param isShuttingDown {@code true} when called from {@link #close()}, which
     * routes the save through {@link #saveLayoutForShutdown()} so a tighter
     * budget can apply while the application is exiting.
     */
    private void autoSave(final boolean isShuttingDown) {
        try {
            if (wasDockEventReceived.getAndSet(false)) {
                logger.debug(
                        "Dock events have been received; " +
                                "attempting to save layout."
                );

                if (isShuttingDown) {
                    saveLayoutForShutdown();
                } else {
                    saveLayout();
                }
            } else {
                logger.debug(
                        "No dock events have been received; " +
                                "will not attempt to save layout."
                );
            }

        } catch (final BentoStateException | RuntimeException e) {
            logger.error(
                    "Could not auto-save docking layout",
                    e
            );
        }
    }

    private void addListeners() {
        for (final Bento bento : bentoProvider.getAllBentos()) {
            if (listenerBentos.add(bento)) {
                bento.events().addEventListener(this);
            }
        }
    }

    private void removeListeners() {
        for (final Bento bento : listenerBentos) {
            bento.events().removeEventListener(this);
        }

        listenerBentos.clear();
    }

    private static final class AutoSaveThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(runnable, "bentofx-layout-auto-save");
            thread.setDaemon(true);
            return thread;
        }
    }
}
