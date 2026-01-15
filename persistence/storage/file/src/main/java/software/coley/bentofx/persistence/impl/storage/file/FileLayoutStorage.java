/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.file;

import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

// TODO BENTO-13: Modify this to use a directory and create and return
//  Optional<LayoutInfo> with a list of layout files.

public class FileLayoutStorage implements LayoutStorage {

    private final File file;

    public FileLayoutStorage(final File file) {
        this.file = file;
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
