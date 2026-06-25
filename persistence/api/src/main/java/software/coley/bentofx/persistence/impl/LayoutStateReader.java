package software.coley.bentofx.persistence.impl;

import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Reads persisted Bento layout state from configured storage.
 *
 * @author Phil Bryant
 */
final class LayoutStateReader {

    private final LayoutCodec layoutCodec;
    private final LayoutStorage layoutStorage;

    LayoutStateReader(
            final LayoutCodec layoutCodec,
            final LayoutStorage layoutStorage
    ) {
        this.layoutCodec = Objects.requireNonNull(layoutCodec);
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
    }

    /**
     * Reads and decodes persisted layout state. This method should run away
     * from the JavaFX application thread because it performs storage and codec
     * operations.
     *
     * @return decoded Bento states.
     * @throws BentoStateException when storage or decoding fails.
     */
    List<BentoState> readLayoutState() throws BentoStateException {
        try (final InputStream in = layoutStorage.openInputStream()) {

            return layoutCodec.decode(in);
        } catch (final IOException e) {

            throw new BentoStateException(
                    "Could not read persisted layout state",
                    e
            );
        }
    }
}
