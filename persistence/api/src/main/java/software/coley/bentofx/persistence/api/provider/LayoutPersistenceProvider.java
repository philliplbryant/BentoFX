/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public interface LayoutPersistenceProvider {

    @NotNull LayoutSaver createLayoutSaver(
            final @NotNull Bento bento,
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull LayoutCodec layoutCodec
    );

    @NotNull LayoutRestorer createLayoutRestorer(
            final @NotNull Bento bento,
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull LayoutCodec layoutCodec,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull ImageProvider imageProvider,
            final @NotNull DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    );
}
