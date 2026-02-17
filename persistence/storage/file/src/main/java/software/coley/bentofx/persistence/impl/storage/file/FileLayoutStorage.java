/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.file;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Implementation of the {@link LayoutStorage} interface for persisting Bento
 * layouts to a file.
 *
 * @author Phil Bryant
 */
public class FileLayoutStorage implements LayoutStorage {

// TODO BENTO-13: Modify this to use a directory and create and return
//  Optional<LayoutInfo> with a list of layout files.

    private final File file;

    /**
     * Creates a {@code FileLayoutStorage} that can be used to persist Bento
     * layout to a file.
     * @param file the {@link File} to which the Bento is to be persisted.
     */
    public FileLayoutStorage(final @NotNull File file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        Files.createDirectories(file.toPath().getParent());
        return Files.newOutputStream(file.toPath());
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return Files.newInputStream(file.toPath());
    }
}
