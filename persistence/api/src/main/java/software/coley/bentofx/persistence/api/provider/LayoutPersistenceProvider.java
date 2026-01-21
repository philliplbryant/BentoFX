/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 */
public interface LayoutPersistenceProvider {

    @NotNull LayoutSaver createLayoutSaver(
            @NotNull final LayoutStorage layoutStorage,
            @NotNull final LayoutCodec layoutCodec
    );

    @NotNull LayoutRestorer createLayoutRestorer(
            @NotNull final LayoutStorage layoutStorage,
            @NotNull final LayoutCodec layoutCodec,
            @NotNull final DockBuilding dockBuilding,
            @NotNull final DockableProvider dockableProvider
    );
}
