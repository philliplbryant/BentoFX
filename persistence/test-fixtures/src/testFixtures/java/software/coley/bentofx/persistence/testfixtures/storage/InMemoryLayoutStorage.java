package software.coley.bentofx.persistence.testfixtures.storage;

import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * {@link LayoutStorage} implementation that persists encoded layout bytes in
 * memory. Intended for use with tests that need a concrete
 * {@link LayoutStorage} without touching the file system or a database.
 *
 * @author Phil Bryant
 */
public class InMemoryLayoutStorage implements LayoutStorage {

    private volatile boolean exists;
    private volatile byte[] bytes;

    /**
     * Creates an empty storage location that does not yet exist.
     */
    public InMemoryLayoutStorage() {
        this(false);
    }

    /**
     * Creates an empty storage location with the requested initial existence
     * state.
     *
     * @param exists initial value returned by {@link #exists()}.
     */
    public InMemoryLayoutStorage(final boolean exists) {
        this(exists, new byte[0]);
    }

    /**
     * Creates a storage location initialized with the supplied bytes. The
     * storage is considered to exist even when the supplied byte array is
     * empty, which allows tests to distinguish between "missing" and
     * "existing but empty" storage.
     *
     * @param bytes initial stored bytes.
     */
    public InMemoryLayoutStorage(final byte[] bytes) {
        this(true, bytes);
    }

    private InMemoryLayoutStorage(
            final boolean exists,
            final byte[] bytes
    ) {
        this.exists = exists;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public synchronized ByteArrayOutputStream openOutputStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                bytes = toByteArray();
                exists = true;
            }
        };
    }

    @Override
    public synchronized InputStream openInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Replaces the current stored bytes and marks the storage as existing.
     *
     * @param bytes stored bytes.
     */
    public synchronized void write(final byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        exists = true;
    }

    /**
     * Clears the stored bytes and marks the storage as missing.
     */
    public synchronized void delete() {
        bytes = new byte[0];
        exists = false;
    }

    /**
     * @return a defensive copy of the currently stored bytes.
     */
    public synchronized byte[] toByteArray() {
        return Arrays.copyOf(bytes, bytes.length);
    }
}
