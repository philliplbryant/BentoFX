package software.coley.bentofx.persistence.impl;

import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Writes captured Bento layout state to configured storage.
 *
 * @author Phil Bryant
 */
final class LayoutStateWriter {

    private final LayoutCodec layoutCodec;
    private final LayoutStorage layoutStorage;

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
        try (final OutputStream out = layoutStorage.openOutputStream()) {

            layoutCodec.encode(bentoStateList, out);
        } catch (final Exception ex) {

            throw new BentoStateException("Failed to encode BentoState", ex);
        }
    }
}
