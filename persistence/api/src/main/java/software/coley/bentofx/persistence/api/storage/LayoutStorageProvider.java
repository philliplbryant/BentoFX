/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.storage;

import org.jetbrains.annotations.NotNull;

public interface LayoutStorageProvider {

    LayoutStorage createLayoutStorage(@NotNull final String fileExtension);
}
