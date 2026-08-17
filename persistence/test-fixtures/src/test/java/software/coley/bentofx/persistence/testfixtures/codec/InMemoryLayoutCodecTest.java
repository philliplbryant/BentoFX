package software.coley.bentofx.persistence.testfixtures.codec;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryLayoutCodecTest {

    private static final String BENTO_IDENTIFIER = "bento:test";

    @Test
    void encodedStatesCanBeDecodedBySameCodecInstance() throws BentoStateException {
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final BentoState state = new BentoStateBuilder(BENTO_IDENTIFIER).build();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        codec.encode(List.of(state), outputStream);

        assertThat(codec.decode(new ByteArrayInputStream(outputStream.toByteArray())))
                .describedAs("codec.decode(new ByteArrayInputStream(outputStream.toByteArray()))")
                .containsExactly(state);
        assertThat(codec.getEncodedStates())
                .describedAs("codec.getEncodedStates()")
                .containsExactly(state);
        assertThat(codec.getEncodeCalls())
                .describedAs("codec.getEncodeCalls()")
                .containsExactly(List.of(state));
    }

    @Test
    void emptyInputDecodesToEmptyStateList() throws BentoStateException {
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();

        assertThat(codec.decode(new ByteArrayInputStream(new byte[0])))
                .describedAs("codec.decode(new ByteArrayInputStream(new byte[0]))")
                .isEmpty();
    }

    @Test
    void tokenFromDifferentCodecInstanceCannotBeDecoded() throws BentoStateException {
        final InMemoryLayoutCodec writer = new InMemoryLayoutCodec();
        final InMemoryLayoutCodec reader = new InMemoryLayoutCodec();
        final BentoState state = new BentoStateBuilder(BENTO_IDENTIFIER).build();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.encode(List.of(state), outputStream);

        assertThatThrownBy(() -> reader.decode(new ByteArrayInputStream(outputStream.toByteArray())))
                .describedAs("decoding a token another codec instance wrote")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("not encoded by this InMemoryLayoutCodec");
    }
}
