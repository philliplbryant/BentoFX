/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.*;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.BentoLayoutRestorer;
import software.coley.bentofx.persistence.impl.BentoLayoutSaver;

import java.util.ServiceLoader;

import static software.coley.bentofx.persistence.api.provider.LayoutStorageProvider.DEFAULT_LAYOUT_NAME;

/**
 * {@code ServiceLoader} compatible Service Provider implementation for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public class BentoLayoutPersistenceProvider
        implements LayoutPersistenceProvider {

    private final @NotNull Bento bento;
    private final @NotNull LayoutCodec layoutCodec;
    private final @NotNull LayoutStorage layoutStorage;
    private final @NotNull LayoutSaver layoutSaver;
    private @Nullable LayoutRestorer layoutRestorer;

    public BentoLayoutPersistenceProvider() {
        this.bento = new Bento();

        final Iterable<LayoutCodecProvider> codecProviders =
                ServiceLoader.load(LayoutCodecProvider.class);
        final LayoutCodecProvider codecProvider =
                codecProviders.iterator().next();
        layoutCodec = codecProvider.createLayoutCodec();

        final Iterable<LayoutStorageProvider> layoutStorageProviders =
                ServiceLoader.load(LayoutStorageProvider.class);
        final LayoutStorageProvider layoutStorageProvider =
                layoutStorageProviders.iterator().next();
        layoutStorage =
                layoutStorageProvider.createLayoutStorage(
                        DEFAULT_LAYOUT_NAME,
                        layoutCodec.getIdentifier()
                );

        this.layoutSaver = new BentoLayoutSaver(
                bento,
                layoutCodec,
                layoutStorage
        );
    }

    @Override
    public @NotNull Bento getBento() {
        return bento;
    }

    @Override
    public @NotNull LayoutSaver getLayoutSaver() {
        return layoutSaver;
    }

    @Override
    public @NotNull LayoutRestorer getLayoutRestorer(
            final @NotNull DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) {
        if (layoutRestorer == null) {

            layoutRestorer = new BentoLayoutRestorer(
                    bento,
                    layoutCodec,
                    layoutStorage,
                    dockableStateProvider,
                    stageIconImageProvider,
                    dockContainerLeafMenuFactoryProvider
            );
        }

        return layoutRestorer;
    }
}
