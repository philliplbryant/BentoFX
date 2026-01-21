/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Application Programming Interface for interacting with a persisted
 * BentoFX layout.
 */
public interface LayoutStorage {

    // TODO BENTO-13: Add a method to return a list of saved layouts?

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
}
