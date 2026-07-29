package software.coley.bentofx.persistence.testfixtures.storage;

import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple in-memory {@link LayoutStorage} for provider-selection tests.
 */
public final class TestLayoutStorage implements LayoutStorage {
    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public OutputStream openOutputStream() {
        return new ByteArrayOutputStream();
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }
}
