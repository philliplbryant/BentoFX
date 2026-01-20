/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.api.storage.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.storage.file.FileLayoutStorage;

import java.io.File;

public class FileLayoutStorageProvider implements LayoutStorageProvider {

    private static final String DEFAULT_BENTO_DIRECTORY =
            System.getProperty("user.home") + "/.bentofx";

    private static final String DEFAULT_BENTO_FILE_NAME = "recent-bento";

    @Override
    public LayoutStorage createLayoutStorage(final @NotNull String fileExtension) {

        final String normalizedFileExtension = fileExtension.startsWith(".") ?
                fileExtension.substring(1) :
                fileExtension;

        final File layoutFile = new File(
                DEFAULT_BENTO_DIRECTORY,
                DEFAULT_BENTO_FILE_NAME + "." + normalizedFileExtension
        );

        return new FileLayoutStorage(layoutFile);
    }
}
