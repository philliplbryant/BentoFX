package software.coley.bentofx.persistence.testfixtures.storage;

import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.*;

/**
 * {@link LayoutStorage} implementation that persists encoded states in-memory.
 * Intended for use with tests.
 *
 * @author Phil Bryant
 */
public class InMemoryLayoutStorage implements LayoutStorage {

    private final boolean exists;
    private byte[] bytes = new byte[0];

    public InMemoryLayoutStorage(boolean exists) {
        this.exists = exists;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public synchronized OutputStream openOutputStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                bytes = toByteArray();
            }
        };
    }

    @Override
    public synchronized InputStream openInputStream() {
        return new ByteArrayInputStream(bytes);
    }
}
