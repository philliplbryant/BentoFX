/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package com.jregw.demo.provider.layout.storage;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.storage.file.FileLayoutStorage;

import java.io.File;

import static com.jregw.demo.ApplicationConstants.DEFAULT_BENTO_DIRECTORY;
import static com.jregw.demo.ApplicationConstants.DEFAULT_BENTO_FILE_NAME;

public class FileLayoutStorageProvider implements LayoutStorageProvider {

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
