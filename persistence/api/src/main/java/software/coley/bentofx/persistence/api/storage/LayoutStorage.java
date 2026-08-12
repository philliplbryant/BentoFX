package software.coley.bentofx.persistence.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Application Programming Interface for interacting with a persisted
 * BentoFX layout.
 * <p>
 * An instance is owned by whichever component it is handed to. The
 * {@link software.coley.bentofx.persistence.api.LayoutSaver} or
 * {@link software.coley.bentofx.persistence.api.LayoutRestorer} that receives a
 * {@code LayoutStorage} closes it when that component is closed, so a single
 * instance must not be shared between the two - closing the saver would close
 * the storage the restorer still reads from. Obtain one instance per component
 * from a
 * {@link software.coley.bentofx.persistence.api.provider.LayoutStorageProvider}.
 *
 * @author Phil Bryant
 */
public interface LayoutStorage extends AutoCloseable {

    /**
     * Returns {@code true} if the stored layout exists; otherwise, returns {@code false}.
     *
     * @return {@code true} if the stored layout exists; otherwise, returns {@code false}.
     */
    boolean exists();

    /**
     * Returns an opened {@link OutputStream} that is expected to be
     * owned/managed by the caller.
     * @return an opened {@link OutputStream} that is expected to be
     * owned/managed by the caller.
     * @throws IOException if an I/O error occurs.
     */
    OutputStream openOutputStream() throws IOException;

    /**
     * Returns an opened {@link InputStream} that is expected to be
     * owned/managed by the caller.
     * @return an opened {@link InputStream} that is expected to be
     * owned/managed by the caller.
     * @throws IOException if an I/O error occurs.
     */
    InputStream openInputStream() throws IOException;

    /**
     * Releases any resources this storage holds. Called by the owning
     * saver or restorer; see the ownership note on this interface before
     * calling it directly.
     */
    @Override
    default void close() {
        // Default no-op. Implementations that own resources should override.
    }
}
