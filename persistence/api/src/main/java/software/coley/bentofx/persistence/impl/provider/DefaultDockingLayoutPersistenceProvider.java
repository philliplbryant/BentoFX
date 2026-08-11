package software.coley.bentofx.persistence.impl.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.DockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceComponentProvider;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.AbstractAutoCloseableLayoutSaver;
import software.coley.bentofx.persistence.impl.DockingLayoutRestorer;
import software.coley.bentofx.persistence.impl.DockingLayoutSaver;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * {@code ServiceLoader} compatible Service Provider implementation for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public class DefaultDockingLayoutPersistenceProvider
        implements DockingLayoutPersistenceProvider {

    private final List<LayoutCodecProvider> layoutCodecProviders;
    private final List<LayoutStorageProvider> layoutStorageProviders;

    public DefaultDockingLayoutPersistenceProvider() {
        this(
                loadProviders(LayoutCodecProvider.class),
                loadProviders(LayoutStorageProvider.class)
        );
    }

    public DefaultDockingLayoutPersistenceProvider(
            final List<LayoutCodecProvider> layoutCodecProviders,
            final List<LayoutStorageProvider> layoutStorageProviders
    ) {
        this.layoutCodecProviders = List.copyOf(
                Objects.requireNonNull(layoutCodecProviders, "layoutCodecProviders")
        );
        this.layoutStorageProviders = List.copyOf(
                Objects.requireNonNull(layoutStorageProviders, "layoutStorageProviders")
        );
    }

    @Override
    public LayoutSaver getLayoutSaver(
            final LayoutPersistenceProfile layoutPersistenceProfile,
            final BentoProvider bentoProvider
    ) throws BentoStateException {

        final LayoutCodecProvider layoutCodecProvider =
                selectProvider(
                        LayoutCodecProvider.class,
                        layoutCodecProviders,
                        layoutPersistenceProfile.codecIdentifier()
                );

        final LayoutStorageProvider layoutStorageProvider =
                selectProvider(
                        LayoutStorageProvider.class,
                        layoutStorageProviders,
                        layoutPersistenceProfile.storageIdentifier()
                );

        final LayoutCodec layoutCodec =
                layoutCodecProvider.getLayoutCodec();

        final LayoutStorage layoutStorage =
                layoutStorageProvider.getLayoutStorage(
                        layoutPersistenceProfile.layoutIdentifier(),
                        layoutCodec.getIdentifier()
                );

        // Construct first, then arm. AbstractAutoCloseableLayoutSaver no longer
        // starts auto-save from its constructor, because that published a
        // half-built object to a scheduler thread and to every Bento event bus.
        return AbstractAutoCloseableLayoutSaver.startAutoSave(
                new DockingLayoutSaver(layoutCodec, layoutStorage, bentoProvider)
        );
    }

    @Override
    public LayoutRestorer getLayoutRestorer(
            final LayoutPersistenceProfile layoutPersistenceProfile,
            final BentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) throws BentoStateException {
        final LayoutCodecProvider layoutCodecProvider =
                selectProvider(
                        LayoutCodecProvider.class,
                        layoutCodecProviders,
                        layoutPersistenceProfile.codecIdentifier()
                );

        final LayoutStorageProvider layoutStorageProvider =
                selectProvider(
                        LayoutStorageProvider.class,
                        layoutStorageProviders,
                        layoutPersistenceProfile.storageIdentifier()
                );

        final LayoutCodec layoutCodec =
                layoutCodecProvider.getLayoutCodec();

        final LayoutStorage layoutStorage =
                layoutStorageProvider.getLayoutStorage(
                        layoutPersistenceProfile.layoutIdentifier(),
                        layoutCodec.getIdentifier()
                );

        return new DockingLayoutRestorer(
                layoutCodec,
                layoutStorage,
                bentoProvider,
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider
        );
    }

    private static <T> List<T> loadProviders(final Class<T> providerType) {
        return ServiceLoader.load(providerType)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }

    private static <T extends LayoutPersistenceComponentProvider> T selectProvider(
            final Class<T> providerType,
            final List<T> providers,
            final @Nullable String requestedIdentifier
    ) throws BentoStateException {
        if (providers.isEmpty()) {
            throw new BentoStateException(
                    "No " + providerType.getSimpleName() + " implementation was found. " +
                            "Add a runtime dependency that provides " + providerType.getSimpleName() + "."
            );
        }

        if (requestedIdentifier != null) {
            return providers.stream()
                    .filter(provider -> provider.getIdentifier().equals(requestedIdentifier))
                    .findFirst()
                    .orElseThrow(() -> new BentoStateException(
                            "No " + providerType.getSimpleName() + " implementation was found for identifier '" +
                                    requestedIdentifier + "'. Available provider identifiers: " + providerIdentifiers(providers)
                    ));
        }

        if (providers.size() == 1) {
            return providers.getFirst();
        }

        final List<T> defaultProviders = providers.stream()
                .filter(LayoutPersistenceComponentProvider::isDefault)
                .toList();

        if (defaultProviders.size() == 1) {
            return defaultProviders.getFirst();
        }

        if (defaultProviders.size() > 1) {
            throw new BentoStateException(
                    "Multiple default " + providerType.getSimpleName() + " implementations were found. " +
                            "Select one explicitly. Available provider identifiers: " + providerIdentifiers(providers)
            );
        }

        throw new BentoStateException(
                "Multiple " + providerType.getSimpleName() + " implementations were found. " +
                        "Select one explicitly or mark exactly one provider as default. " +
                        "Available provider identifiers: " + providerIdentifiers(providers)
        );
    }

    private static String providerIdentifiers(
            final List<? extends LayoutPersistenceComponentProvider> providers
    ) {
        return providers.stream()
                .map(LayoutPersistenceComponentProvider::getIdentifier)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
