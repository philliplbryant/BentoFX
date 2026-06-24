package software.coley.bentofx.persistence.testfixtures.codec;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadRecordingLayoutCodecTest {

    @Test
    void encodeRecordsThreadAndStates() throws BentoStateException {
        final ThreadRecordingLayoutCodec codec = new ThreadRecordingLayoutCodec();
        final BentoState state = new BentoStateBuilder("bento:test").build();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Thread currentThread = Thread.currentThread();

        codec.encode(List.of(state), outputStream);

        assertThat(codec.getEncodeThread())
                .isSameAs(currentThread);
        assertThat(codec.getEncodedStates())
                .containsExactly(state);
        assertThat(outputStream.toByteArray())
                .isNotEmpty();
    }

    @Test
    void decodeRecordsThreadAndReturnsSeededStates() throws BentoStateException {
        final ThreadRecordingLayoutCodec codec = new ThreadRecordingLayoutCodec();
        final BentoState state = new BentoStateBuilder("bento:test").build();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Thread currentThread = Thread.currentThread();

        codec.writeEncoded(List.of(state), outputStream);

        assertThat(codec.decode(new ByteArrayInputStream(outputStream.toByteArray())))
                .containsExactly(state);
        assertThat(codec.getDecodeThread())
                .isSameAs(currentThread);
    }
}
