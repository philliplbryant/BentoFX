package software.coley.bentofx.persistence.testfixtures.codec;

import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test codec that records the threads used to encode and decode persisted state.
 */
public final class ThreadRecordingLayoutCodec implements LayoutCodec {
    private final AtomicReference<Thread> encodeThread = new AtomicReference<>();
    private final AtomicReference<Thread> decodeThread = new AtomicReference<>();
    private List<BentoState> encodedStates = List.of();
    private List<BentoState> decodedStates = List.of();

    @Override
    public String getIdentifier() {
        return "thread-recording";
    }

    @Override
    public synchronized void encode(
            final List<BentoState> bentoStates,
            final OutputStream outputStream
    ) throws BentoStateException {
        encodeThread.set(Thread.currentThread());
        encodedStates = List.copyOf(bentoStates);
        writeMarker(outputStream);
    }

    @Override
    public synchronized List<BentoState> decode(
            final InputStream inputStream
    ) throws BentoStateException {
        decodeThread.set(Thread.currentThread());
        return new ArrayList<>(decodedStates);
    }

    /**
     * Seeds the states returned by {@link #decode(InputStream)} and writes a marker to storage.
     *
     * @param bentoStates
     *        States to return when decoding.
     * @param outputStream
     *        Output stream to mark as written.
     *
     * @throws BentoStateException
     *         When writing the marker fails.
     */
    public synchronized void writeEncoded(
            final List<BentoState> bentoStates,
            final OutputStream outputStream
    ) throws BentoStateException {
        decodedStates = List.copyOf(bentoStates);
        writeMarker(outputStream);
    }

    public Thread getEncodeThread() {
        return encodeThread.get();
    }

    public Thread getDecodeThread() {
        return decodeThread.get();
    }

    public synchronized List<BentoState> getEncodedStates() {
        return encodedStates;
    }

    private static void writeMarker(final OutputStream outputStream) throws BentoStateException {
        try {
            outputStream.write(1);
        } catch (final IOException e) {
            throw new BentoStateException("Could not write test codec marker.", e);
        }
    }
}
