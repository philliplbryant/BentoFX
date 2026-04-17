package software.coley.bentofx.persistence.impl.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.*;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.BentoLayoutRestorer;
import software.coley.bentofx.persistence.impl.BentoLayoutSaver;

import java.util.ServiceLoader;

/**
 * {@code ServiceLoader} compatible Service Provider implementation for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public class BentoLayoutPersistenceProvider
        implements LayoutPersistenceProvider {

    private final LayoutCodecProvider layoutCodecProvider;
    private final LayoutStorageProvider layoutStorageProvider;

    public BentoLayoutPersistenceProvider() {

        final Iterable<LayoutCodecProvider> codecProviders =
                ServiceLoader.load(LayoutCodecProvider.class);
        layoutCodecProvider =
                codecProviders.iterator().next();

        final Iterable<LayoutStorageProvider> layoutStorageProviders =
                ServiceLoader.load(LayoutStorageProvider.class);
        layoutStorageProvider =
                layoutStorageProviders.iterator().next();
    }

    @Override
    public LayoutSaver getLayoutSaver(
            final BentoProvider bentoProvider,
            final String layoutIdentifier
    ) {

        final LayoutCodec layoutCodec =
                layoutCodecProvider.createLayoutCodec();

        final LayoutStorage layoutStorage =
                layoutStorageProvider.createLayoutStorage(
                        layoutIdentifier,
                        layoutCodec.getIdentifier()
                );

        return new BentoLayoutSaver(
                bentoProvider,
                layoutCodec,
                layoutStorage
        );
    }

    @Override
    public LayoutRestorer getLayoutRestorer(
            final BentoProvider bentoProvider,
            final String layoutIdentifier,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) {
        final LayoutCodec layoutCodec =
                layoutCodecProvider.createLayoutCodec();

        final LayoutStorage layoutStorage =
                layoutStorageProvider.createLayoutStorage(
                        layoutIdentifier,
                        layoutCodec.getIdentifier()
                );

        return new BentoLayoutRestorer(
                layoutCodec,
                layoutStorage,
                bentoProvider,
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider
        );
    }
}
