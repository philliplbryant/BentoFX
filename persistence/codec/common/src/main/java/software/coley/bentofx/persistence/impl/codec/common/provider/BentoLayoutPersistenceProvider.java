/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.DockableProvider;
import software.coley.bentofx.persistence.api.provider.ImageProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutRestorer;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutSaver;

/**
 * {@code ServiceLoader} compatible Service Provider implementation for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public class BentoLayoutPersistenceProvider
        implements LayoutPersistenceProvider {

    @Override
    public @NotNull LayoutSaver createLayoutSaver(
            final @NotNull Bento bento,
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull LayoutCodec layoutCodec
    ) {
        return new BentoLayoutSaver(bento, layoutStorage, layoutCodec);
    }

    @Override
    public @NotNull LayoutRestorer createLayoutRestorer(
            final @NotNull Bento bento,
            @NotNull LayoutStorage layoutStorage,
            @NotNull LayoutCodec layoutCodec,
            @NotNull DockableProvider dockableProvider,
            @NotNull ImageProvider imageProvider
    ) {
        return new BentoLayoutRestorer(
                bento,
                layoutStorage,
                layoutCodec,
                dockableProvider,
                imageProvider
        );
    }
}
