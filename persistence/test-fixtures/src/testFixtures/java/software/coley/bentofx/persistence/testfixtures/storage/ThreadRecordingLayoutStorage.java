package software.coley.bentofx.persistence.testfixtures.storage;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test storage that records the threads used to inspect and open persisted layout streams.
 */
public final class ThreadRecordingLayoutStorage implements LayoutStorage {
    private final AtomicReference<@Nullable Thread> existsThread = new AtomicReference<>();
    private final AtomicReference<@Nullable Thread> openInputStreamThread = new AtomicReference<>();
    private final AtomicReference<@Nullable Thread> openOutputStreamThread = new AtomicReference<>();
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Override
    public boolean exists() {
        existsThread.set(Thread.currentThread());
        return outputStream.size() > 0;
    }

    @Override
    public synchronized OutputStream openOutputStream() {
        openOutputStreamThread.set(Thread.currentThread());
        outputStream.reset();
        return outputStream;
    }

    @Override
    public synchronized InputStream openInputStream() {
        openInputStreamThread.set(Thread.currentThread());
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public @Nullable Thread getExistsThread() {
        return existsThread.get();
    }

    public @Nullable Thread getOpenInputStreamThread() {
        return openInputStreamThread.get();
    }

    public @Nullable Thread getOpenOutputStreamThread() {
        return openOutputStreamThread.get();
    }

    public synchronized byte[] toByteArray() {
        return outputStream.toByteArray();
    }
}
