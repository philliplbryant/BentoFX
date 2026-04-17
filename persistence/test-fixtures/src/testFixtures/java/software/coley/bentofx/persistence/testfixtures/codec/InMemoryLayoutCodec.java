package software.coley.bentofx.persistence.testfixtures.codec;

import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.BentoState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link LayoutCodec} implementation that encodes all states to an
 * in-memory list. Intended for use with tests.
 *
 * @author Phil Bryant
 */
public class InMemoryLayoutCodec implements LayoutCodec {

    private final List<BentoState> encodedStates = new ArrayList<>();

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public void encode(
            List<BentoState> bentoStates,
            OutputStream outputStream
    ) throws BentoStateException {
        encodedStates.addAll(bentoStates);
        try {
            outputStream.write(new byte[]{1, 2, 3});
        } catch (IOException e) {
            throw new BentoStateException(
                    "Could not write encoded Bento states output stream",
                    e
            );
        }
    }

    @Override
    public List<BentoState> decode(InputStream inputStream) {
        return encodedStates.stream().toList();
    }

    public List<BentoState> getEncodedStates() {
        return encodedStates.stream().toList();
    }
}
