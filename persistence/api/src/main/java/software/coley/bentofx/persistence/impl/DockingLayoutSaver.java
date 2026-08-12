package software.coley.bentofx.persistence.impl;

import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.util.List;
import java.util.Objects;

import static software.coley.bentofx.persistence.impl.PersistenceThreading.callOffFxThread;

/**
 * Automatically saves the layout of all {@link Bento}s into {@link BentoState},
 * encodes them via a {@link LayoutCodec}, and persists them via a
 * {@link LayoutStorage}.
 *
 * @author Phil Bryant
 */
public class DockingLayoutSaver extends AbstractAutoCloseableLayoutSaver {

    private final BentoLayoutStateCaptor bentoLayoutStateCaptor;
    private final LayoutStateWriter layoutStateWriter;

    /**
     * Creates a {@link DockingLayoutSaver}
     *
     * @param layoutCodec   the {@link LayoutCodec} to use to encode the persisted
     *                      layout.
     * @param layoutStorage the {@link LayoutStorage} to use to write the
     *                      persisted layout. This saver takes ownership of it
     *                      and closes it from {@link #close()}, so the same
     *                      instance must not also be given to a
     *                      {@link software.coley.bentofx.persistence.api.LayoutRestorer}.
     * @param bentoProvider the {@link BentoProvider} to use to get {@link Bento}
     *                      instances from their identifiers.
     */
    public DockingLayoutSaver(
            final LayoutCodec layoutCodec,
            final LayoutStorage layoutStorage,
            final BentoProvider bentoProvider
    ) {
        this(
                bentoProvider,
                new BentoLayoutStateCaptor(bentoProvider),
                new LayoutStateWriter(layoutCodec, layoutStorage)
        );
    }

    DockingLayoutSaver(
            final BentoProvider bentoProvider,
            final BentoLayoutStateCaptor bentoLayoutStateCaptor,
            final LayoutStateWriter layoutStateWriter
    ) {
        super(bentoProvider);
        this.bentoLayoutStateCaptor = Objects.requireNonNull(bentoLayoutStateCaptor);
        this.layoutStateWriter = Objects.requireNonNull(layoutStateWriter);
    }

    @Override
    public void saveLayout() throws BentoStateException {
        saveLayout(PersistenceThreading.FX_CAPTURE_TIMEOUT_MILLIS);
    }

    @Override
    protected void saveLayoutForShutdown() throws BentoStateException {
        // A shorter budget while the application is exiting: the thread calling
        // close() may not be a daemon, so waiting the full capture budget could
        // hold up shutdown. Losing the final save beats wedging the exit.
        saveLayout(PersistenceThreading.FX_CLOSE_TIMEOUT_MILLIS);
    }

    /**
     * Captures the layout on the JavaFX application thread, then encodes and
     * writes it away from that thread.
     *
     * @param fxTimeoutMillis how long to wait for the JavaFX application thread
     * to capture the layout.
     * @throws BentoStateException when capturing, encoding or writing fails.
     */
    private void saveLayout(final long fxTimeoutMillis)
            throws BentoStateException {
        final List<BentoState> bentoStateList =
                PersistenceThreading.callOnFxThread(
                        bentoLayoutStateCaptor::captureBentoStates,
                        fxTimeoutMillis
                );

        callOffFxThread(() -> {
            layoutStateWriter.writeLayout(bentoStateList);
            return Boolean.TRUE;
        });
    }


    @Override
    public void close() {
        try {
            super.close();
        } finally {
            layoutStateWriter.close();
        }
    }
}
