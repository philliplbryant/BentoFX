package software.coley.bentofx.persistence.impl;

import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Writes captured Bento layout state to configured storage.
 *
 * @author Phil Bryant
 */
final class LayoutStateWriter implements AutoCloseable {

    private final LayoutCodec layoutCodec;
    private final LayoutStorage layoutStorage;
    private final AtomicBoolean closed = new AtomicBoolean();

    LayoutStateWriter(
            final LayoutCodec layoutCodec,
            final LayoutStorage layoutStorage
    ) {
        this.layoutCodec = Objects.requireNonNull(layoutCodec);
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
    }

    /**
     * Encodes and writes captured layout state. This method should run away
     * from the JavaFX application thread because it performs codec and storage
     * operations.
     *
     * @param bentoStateList captured Bento states.
     * @throws BentoStateException when encoding or storage fails.
     */
    void writeLayout(final List<BentoState> bentoStateList)
            throws BentoStateException {

        // Encoded in full before storage is opened. Handing the codec the
        // storage stream meant a codec failure part way through had already
        // replaced what was stored before, since closing the stream is what
        // commits it.
        final byte[] encoded = encode(bentoStateList);

        try (final OutputStream out = layoutStorage.openOutputStream()) {

            out.write(encoded);
        } catch (final IOException e) {

            throw new BentoStateException(
                    "Could not write persisted layout state",
                    e
            );
        }
    }

    /**
     * {@return the encoded layout.}
     *
     * @param bentoStateList captured Bento states.
     * @throws BentoStateException when encoding fails.
     */
    private byte[] encode(final List<BentoState> bentoStateList)
            throws BentoStateException {

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        layoutCodec.encode(bentoStateList, buffer);

        return buffer.toByteArray();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            layoutStorage.close();
        }
    }
}
