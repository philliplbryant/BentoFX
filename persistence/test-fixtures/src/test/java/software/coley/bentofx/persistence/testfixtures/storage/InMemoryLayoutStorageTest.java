package software.coley.bentofx.persistence.testfixtures.storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLayoutStorageTest {

    @Test
    void defaultStorageDoesNotExist() {
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage();

        assertThat(storage.exists())
                .isFalse();
        assertThat(storage.toByteArray())
                .isEmpty();
    }

    @Test
    void storageExistsAfterOutputStreamIsClosed() throws IOException {
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage();

        try (OutputStream outputStream = storage.openOutputStream()) {
            outputStream.write(new byte[]{1, 2, 3});
        }

        assertThat(storage.exists())
                .isTrue();
        assertThat(storage.openInputStream().readAllBytes())
                .containsExactly(1, 2, 3);
    }

    @Test
    void writeCopiesInputBytes() {
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage();
        final byte[] bytes = {1, 2, 3};

        storage.write(bytes);
        bytes[0] = 9;

        assertThat(storage.toByteArray())
                .containsExactly(1, 2, 3);
    }

    @Test
    void toByteArrayReturnsDefensiveCopy() {
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage(new byte[]{1, 2, 3});

        final byte[] copy = storage.toByteArray();
        copy[0] = 9;

        assertThat(storage.toByteArray())
                .containsExactly(1, 2, 3);
    }

    @Test
    void deleteClearsStorage() {
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage(new byte[]{1, 2, 3});

        storage.delete();

        assertThat(storage.exists())
                .isFalse();
        assertThat(storage.toByteArray())
                .isEmpty();
    }
}
