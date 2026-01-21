/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.persistence.api.provider.DockableProvider;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutRestorer;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutSaver;

/**
 * {@code ServiceLoader} compatible Service Provider implementation for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 */
public class BentoLayoutPersistenceProvider
        implements LayoutPersistenceProvider {

    @Override
    public @NotNull LayoutSaver createLayoutSaver(
            @NotNull LayoutStorage layoutStorage,
            @NotNull LayoutCodec layoutCodec
    ) {
        return new BentoLayoutSaver(layoutStorage, layoutCodec);
    }

    @Override
    public @NotNull LayoutRestorer createLayoutRestorer(
            @NotNull LayoutStorage layoutStorage,
            @NotNull LayoutCodec layoutCodec,
            @NotNull DockBuilding dockBuilding,
            @NotNull DockableProvider dockableProvider
    ) {
        return new BentoLayoutRestorer(
                layoutStorage,
                layoutCodec,
                dockBuilding,
                dockableProvider
        );
    }
}
