/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

public interface LayoutStorageProvider {

    LayoutStorage createLayoutStorage(
            @NotNull final String fileExtension
    );
}
