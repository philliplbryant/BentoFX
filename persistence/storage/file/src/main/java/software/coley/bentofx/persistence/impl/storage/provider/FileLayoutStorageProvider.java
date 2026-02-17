/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.storage.file.FileLayoutStorage;

import java.io.File;

/**
 * Implementation of the {@link LayoutStorageProvider} interface for persisting
 * Bento layouts to a file.
 *
 * @author Phil Bryant
 */
public class FileLayoutStorageProvider implements LayoutStorageProvider {

    private static final String DEFAULT_BENTO_DIRECTORY =
            System.getProperty("user.home") + "/.bentofx";

    @Override
    public LayoutStorage createLayoutStorage(
            final @NotNull String bentoIdentifier,
            final @NotNull String layoutIdentifier,
            final @NotNull String codecIdentifier
    ) {

        final String normalizedFileExtension = codecIdentifier.startsWith(".") ?
                codecIdentifier.substring(1) :
                codecIdentifier;

        final File layoutFile = new File(
                DEFAULT_BENTO_DIRECTORY,
                bentoIdentifier + "-" + layoutIdentifier + "." + normalizedFileExtension
        );

        return new FileLayoutStorage(layoutFile);
    }
}
