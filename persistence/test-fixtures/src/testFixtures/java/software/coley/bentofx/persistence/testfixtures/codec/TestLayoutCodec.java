package software.coley.bentofx.persistence.testfixtures.codec;

import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Simple {@link LayoutCodec} implementation for provider-selection tests.
 */
public record TestLayoutCodec(String identifier) implements LayoutCodec {
    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void encode(
            final List<BentoState> bentoStates,
            final OutputStream outputStream
    ) throws BentoStateException {
        // no-op
    }

    @Override
    public List<BentoState> decode(
            final InputStream inputStream
    ) throws BentoStateException {
        return List.of();
    }
}
