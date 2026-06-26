package software.coley.bentofx.persistence.impl;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@NullMarked
class LayoutStateWriterTest {

    private static final String CODEC_IDENTIFIER = "test";
    private static final String ENCODED_LAYOUT = "encoded-layout";
    private static final String LAYOUT_IDENTIFIER = "main";
    private static final String CODEC_FAILURE_MESSAGE = "codec failed";
    private static final String STORAGE_FAILURE_MESSAGE = "storage failed";
    private static final String WRITE_FAILURE_MESSAGE = "Failed to encode BentoState";
    private static final String WRITE_EXCEPTION_DESCRIPTION =
            "exception thrown by LayoutStateWriter.writeLayout(List.of())";
    private static final String OUTPUT_STREAM_CONTENT_DESCRIPTION = "storage output stream content";

    @Test
    void writesEncodedStateToStorage() throws BentoStateException {
        final RecordingLayoutCodec codec = new RecordingLayoutCodec();
        final BentoState bentoState = new BentoStateBuilder(LAYOUT_IDENTIFIER)
                .build();

        try (final InMemoryLayoutStorage storage = new InMemoryLayoutStorage()) {
            new LayoutStateWriter(codec, storage).writeLayout(List.of(bentoState));

            assertThat(codec.getEncodedBentoStates())
                    .describedAs("codec.getEncodedBentoStates()")
                    .containsExactly(bentoState);
            assertThat(storage.getOutputStreamContent())
                    .describedAs(OUTPUT_STREAM_CONTENT_DESCRIPTION)
                    .isEqualTo(ENCODED_LAYOUT);
        }
    }

    @Test
    void wrapsEncodingFailures() {
        final RecordingLayoutCodec codec = new RecordingLayoutCodec();
        final BentoStateException expectedCause = new BentoStateException(CODEC_FAILURE_MESSAGE);
        codec.setEncodeException(expectedCause);

        try (final InMemoryLayoutStorage storage = new InMemoryLayoutStorage()) {
            assertThatThrownBy(() ->
                    new LayoutStateWriter(codec, storage).writeLayout(List.of())
            )
                    .describedAs(WRITE_EXCEPTION_DESCRIPTION)
                    .isInstanceOf(BentoStateException.class)
                    .hasMessage(WRITE_FAILURE_MESSAGE)
                    .hasCause(expectedCause);
        }
    }

    @Test
    void wrapsStorageFailures() {
        final RecordingLayoutCodec codec = new RecordingLayoutCodec();
        final IOException expectedCause = new IOException(STORAGE_FAILURE_MESSAGE);

        try (final InMemoryLayoutStorage storage = new InMemoryLayoutStorage()) {
            storage.setOpenOutputStreamException(expectedCause);

            assertThatThrownBy(() ->
                    new LayoutStateWriter(codec, storage).writeLayout(List.of())
            )
                    .describedAs(WRITE_EXCEPTION_DESCRIPTION + " when storage fails")
                    .isInstanceOf(BentoStateException.class)
                    .hasMessage(WRITE_FAILURE_MESSAGE)
                    .hasCause(expectedCause);
        }
    }


    @Test
    void closeClosesStorageOnce() {
        final RecordingLayoutCodec codec = new RecordingLayoutCodec();
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage();
        final LayoutStateWriter writer = new LayoutStateWriter(codec, storage);

        writer.close();
        writer.close();

        assertThat(storage.getCloseCount())
                .describedAs("storage.getCloseCount()")
                .isEqualTo(1);
    }

    private static final class RecordingLayoutCodec implements LayoutCodec {

        private @Nullable List<BentoState> encodedBentoStates;
        private @Nullable BentoStateException encodeException;

        @Override
        public String getIdentifier() {
            return CODEC_IDENTIFIER;
        }

        @Override
        public void encode(
                final List<BentoState> bentoStates,
                final OutputStream outputStream
        ) throws BentoStateException {
            if (encodeException != null) {
                throw encodeException;
            }

            encodedBentoStates = List.copyOf(bentoStates);
            try {
                outputStream.write(ENCODED_LAYOUT.getBytes(StandardCharsets.UTF_8));
            } catch (final IOException e) {
                throw new BentoStateException("Could not write test layout", e);
            }
        }

        @Override
        public List<BentoState> decode(final InputStream inputStream) {
            return List.of();
        }

        @Nullable
        List<BentoState> getEncodedBentoStates() {
            return encodedBentoStates;
        }

        void setEncodeException(final @Nullable BentoStateException encodeException) {
            this.encodeException = encodeException;
        }
    }

    private static final class InMemoryLayoutStorage implements LayoutStorage {

        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private @Nullable IOException openOutputStreamException;
        private int closeCount;

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            if (openOutputStreamException != null) {
                throw openOutputStreamException;
            }
            return outputStream;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void close() {
            closeCount++;
        }

        String getOutputStreamContent() {
            return outputStream.toString(StandardCharsets.UTF_8);
        }

        int getCloseCount() {
            return closeCount;
        }

        void setOpenOutputStreamException(
                final @Nullable IOException openOutputStreamException
        ) {
            this.openOutputStreamException = openOutputStreamException;
        }
    }
}
