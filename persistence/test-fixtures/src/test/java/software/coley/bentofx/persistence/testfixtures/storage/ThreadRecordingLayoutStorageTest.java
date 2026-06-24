package software.coley.bentofx.persistence.testfixtures.storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadRecordingLayoutStorageTest {

    @Test
    void existsRecordsCallingThread() {
        final ThreadRecordingLayoutStorage storage = new ThreadRecordingLayoutStorage();
        final Thread currentThread = Thread.currentThread();

        assertThat(storage.exists())
                .isFalse();

        assertThat(storage.getExistsThread())
                .isSameAs(currentThread);
    }

    @Test
    void openOutputStreamRecordsCallingThreadAndStoresBytes() throws IOException {
        final ThreadRecordingLayoutStorage storage = new ThreadRecordingLayoutStorage();
        final Thread currentThread = Thread.currentThread();

        try (OutputStream outputStream = storage.openOutputStream()) {
            outputStream.write(new byte[]{1, 2, 3});
        }

        assertThat(storage.getOpenOutputStreamThread())
                .isSameAs(currentThread);
        assertThat(storage.toByteArray())
                .containsExactly(1, 2, 3);
    }

    @Test
    void openInputStreamRecordsCallingThreadAndReadsStoredBytes() throws IOException {
        final ThreadRecordingLayoutStorage storage = new ThreadRecordingLayoutStorage();
        final Thread currentThread = Thread.currentThread();

        try (OutputStream outputStream = storage.openOutputStream()) {
            outputStream.write(new byte[]{1, 2, 3});
        }

        assertThat(storage.openInputStream().readAllBytes())
                .containsExactly(1, 2, 3);
        assertThat(storage.getOpenInputStreamThread())
                .isSameAs(currentThread);
    }
}
