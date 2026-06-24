package software.coley.bentofx.persistence.testfixtures.storage;

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
    private final AtomicReference<Thread> existsThread = new AtomicReference<>();
    private final AtomicReference<Thread> openInputStreamThread = new AtomicReference<>();
    private final AtomicReference<Thread> openOutputStreamThread = new AtomicReference<>();
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

    public Thread getExistsThread() {
        return existsThread.get();
    }

    public Thread getOpenInputStreamThread() {
        return openInputStreamThread.get();
    }

    public Thread getOpenOutputStreamThread() {
        return openOutputStreamThread.get();
    }

    public synchronized byte[] toByteArray() {
        return outputStream.toByteArray();
    }
}
