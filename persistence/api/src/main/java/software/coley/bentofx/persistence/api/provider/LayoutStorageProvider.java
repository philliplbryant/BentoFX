/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutStorage} implementations.
 *
 * @author Phil Bryant
 */
public interface LayoutStorageProvider {

    /**
     * The name used to identify the default layout.
     */
    String DEFAULT_LAYOUT_NAME = "recent-bento";

    /**
     * Creates a {@link LayoutStorage} that can be used to persist a Bento
     * layout.
     * @return a {@link LayoutStorage} that can be used to persist a Bento
     * layout.
     */
    LayoutStorage createLayoutStorage(
            final @NotNull String layoutIdentifier,
            final @NotNull String codecIdentifier
    );
}
