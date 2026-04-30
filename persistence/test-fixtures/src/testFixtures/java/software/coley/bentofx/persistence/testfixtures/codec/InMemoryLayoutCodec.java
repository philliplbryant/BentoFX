package software.coley.bentofx.persistence.testfixtures.codec;

import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.BentoState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link LayoutCodec} implementation that encodes layout states to small
 * in-memory tokens. Intended for tests that need a concrete
 * {@link LayoutCodec} without requiring JSON, XML, or another real
 * persistence format.
 * <p>
 * The encoded bytes are meaningful to the codec instance that wrote them. Use
 * the same instance when testing a save/restore round trip with an
 * in-memory storage fixture.
 *
 * @author Phil Bryant
 */
public class InMemoryLayoutCodec implements LayoutCodec {

    private static final String IDENTIFIER = "memory";
    private static final String TOKEN_PREFIX = "bento-state-set-";

    private final Map<String, List<BentoState>> encodedStateSets =
            new HashMap<>();
    private final List<List<BentoState>> encodeCalls = new ArrayList<>();
    private int nextToken = 0;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public synchronized void encode(
            final List<BentoState> bentoStates,
            final OutputStream outputStream
    ) throws BentoStateException {
        final List<BentoState> copy = List.copyOf(bentoStates);
        final String token = TOKEN_PREFIX + nextToken++;

        encodedStateSets.put(token, copy);
        encodeCalls.add(copy);

        try {
            outputStream.write(token.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new BentoStateException(
                    "Could not write encoded Bento states output stream",
                    e
            );
        }
    }

    @Override
    public synchronized List<BentoState> decode(
            final InputStream inputStream
    ) throws BentoStateException {
        final String token;

        try {
            token = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new BentoStateException(
                    "Could not read encoded Bento states input stream",
                    e
            );
        }

        if (token.isEmpty()) {
            return List.of();
        }

        final List<BentoState> bentoStates = encodedStateSets.get(token);
        if (bentoStates == null) {
            throw new BentoStateException(
                    "Input stream was not encoded by this InMemoryLayoutCodec"
            );
        }

        return bentoStates;
    }

    /**
     * Seeds the codec with states and writes the matching encoded token to the
     * supplied output stream. This is useful when testing restore-only code.
     *
     * @param bentoStates states returned by later {@link #decode(InputStream)}
     * calls for the written bytes.
     * @param outputStream output stream to receive the encoded token.
     * @throws BentoStateException on error.
     */
    public void writeEncoded(
            final List<BentoState> bentoStates,
            final OutputStream outputStream
    ) throws BentoStateException {
        encode(bentoStates, outputStream);
    }

    /**
     * @return states from the most recent encode call, or an empty list if this
     * codec has not encoded anything.
     */
    public synchronized List<BentoState> getEncodedStates() {
        if (encodeCalls.isEmpty()) {
            return List.of();
        }

        return encodeCalls.getLast();
    }

    /**
     * @return all encode calls, in call order.
     */
    public synchronized List<List<BentoState>> getEncodeCalls() {
        return encodeCalls.stream()
                .map(List::copyOf)
                .toList();
    }
}
