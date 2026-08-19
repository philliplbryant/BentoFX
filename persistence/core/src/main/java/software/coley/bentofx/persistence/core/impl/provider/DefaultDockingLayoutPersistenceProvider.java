package software.coley.bentofx.persistence.core.impl.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.core.api.BentoStateException;
import software.coley.bentofx.persistence.core.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.core.api.LayoutRestorer;
import software.coley.bentofx.persistence.core.api.LayoutSaver;
import software.coley.bentofx.persistence.core.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.core.api.provider.BentoProvider;
import software.coley.bentofx.persistence.core.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.core.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.core.api.provider.DockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.core.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.core.api.provider.LayoutPersistenceComponentProvider;
import software.coley.bentofx.persistence.core.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.core.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.core.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.core.impl.AbstractAutoCloseableLayoutSaver;
import software.coley.bentofx.persistence.core.impl.DockingLayoutRestorer;
import software.coley.bentofx.persistence.core.impl.DockingLayoutSaver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    /**
     * Constructs a provider whose codec and storage providers are discovered
     * once, now, with {@code ServiceLoader}. A provider registered after this
     * runs will not be seen.
     *
     * <p>Discovery uses the class loader of the service interface, so it follows
     * the module graph that declares the {@code uses} clauses rather than whatever
     * thread happens to construct this. An implementation living in a class loader
     * that this module cannot see - a container or plugin loader - will not be
     * found, and has to be passed to
     * {@link #DefaultDockingLayoutPersistenceProvider(List, List)} instead.</p>
     */
    public DefaultDockingLayoutPersistenceProvider() {
        this(
                loadProviders(LayoutCodecProvider.class),
                loadProviders(LayoutStorageProvider.class)
        );
    }

    /**
     * Constructor taking the providers explicitly, bypassing
     * {@code ServiceLoader} discovery.
     *
     * @param layoutCodecProviders the {@link LayoutCodecProvider}s to choose a
     * codec from.
     * @param layoutStorageProviders the {@link LayoutStorageProvider}s to choose
     * a storage from.
     */
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

        // A storage instance of this saver's own, not one shared with the
        // restorer this provider hands out for the same profile. Whichever
        // component receives a LayoutStorage closes it (see
        // LayoutStorage.close()), so sharing one would let closing the saver
        // shut the storage the restorer still reads from. Both calls name the
        // same layout, so they still address the same persisted layout.
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

        // This restorer's own storage instance, for the ownership reason spelled
        // out in getLayoutSaver above.
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

    @Override
    public void saveLayout(
            final LayoutPersistenceProfile layoutPersistenceProfile,
            final BentoProvider bentoProvider
    ) throws BentoStateException {

        final LayoutCodec layoutCodec =
                selectCodec(layoutPersistenceProfile).getLayoutCodec();

        final LayoutStorage layoutStorage =
                selectStorageProvider(layoutPersistenceProfile).getLayoutStorage(
                        layoutPersistenceProfile.layoutIdentifier(),
                        layoutCodec.getIdentifier()
                );

        // Deliberately not enabling startAutoSave because a one-shot save must not
        // register a listener on any Bento or start a scheduler it would then have
        // to take down. Closing releases the storage, and its own final save is a
        // no-op because no dock event can have reached a saver nothing listens with.
        try (final DockingLayoutSaver layoutSaver = new DockingLayoutSaver(
                layoutCodec,
                layoutStorage,
                bentoProvider,
                layoutPersistenceProfile.displayName()
        )) {
            layoutSaver.saveLayout();
        }
    }

    @Override
    public List<String> getStoredLayoutIdentifiers(
            final LayoutPersistenceProfile layoutPersistenceProfile
    ) throws BentoStateException {

        return selectStorageProvider(layoutPersistenceProfile)
                .getLayoutIdentifiers(codecIdentifier(layoutPersistenceProfile));
    }

    @Override
    public List<LayoutPersistenceProfile> getStoredLayouts(
            final LayoutPersistenceProfile layoutPersistenceProfile
    ) throws BentoStateException {

        final LayoutCodec layoutCodec =
                selectCodec(layoutPersistenceProfile).getLayoutCodec();
        final LayoutStorageProvider layoutStorageProvider =
                selectStorageProvider(layoutPersistenceProfile);

        final List<LayoutPersistenceProfile> layouts = new ArrayList<>();

        final String codecIdentifier = layoutCodec.getIdentifier();

        for (final String layoutIdentifier :
                layoutStorageProvider.getLayoutIdentifiers(codecIdentifier)) {

            layouts.add(new LayoutPersistenceProfile(
                    layoutIdentifier,
                    layoutPersistenceProfile.codecIdentifier(),
                    layoutPersistenceProfile.storageIdentifier(),
                    readDisplayName(
                            layoutStorageProvider,
                            layoutCodec,
                            layoutIdentifier
                    )
            ));
        }

        return layouts;
    }

    /**
     * {@return the display name stored with a layout, or {@code null} when it
     * was saved without one.}
     *
     * <p>The display name lives inside the layout, so reading it means decoding
     * the layout. This opens its storage, decodes, and closes.</p>
     *
     * @param layoutStorageProvider the storage the layout is held in.
     * @param layoutCodec the codec the layout was written with.
     * @param layoutIdentifier identifies the layout to read.
     * @throws BentoStateException when the layout cannot be read or decoded.
     */
    private static @Nullable String readDisplayName(
            final LayoutStorageProvider layoutStorageProvider,
            final LayoutCodec layoutCodec,
            final String layoutIdentifier
    ) throws BentoStateException {

        try (final LayoutStorage layoutStorage =
                     layoutStorageProvider.getLayoutStorage(
                             layoutIdentifier,
                             layoutCodec.getIdentifier()
                     );
             final InputStream inputStream = layoutStorage.openInputStream()) {

            return layoutCodec.decode(inputStream).displayName();
        } catch (final IOException e) {
            throw new BentoStateException(
                    "Could not read the stored layout '"
                            + layoutIdentifier + "'.",
                    e
            );
        }
    }

    @Override
    public boolean isLayoutStored(
            final LayoutPersistenceProfile layoutPersistenceProfile
    ) throws BentoStateException {

        return selectStorageProvider(layoutPersistenceProfile).isLayoutStored(
                layoutPersistenceProfile.layoutIdentifier(),
                codecIdentifier(layoutPersistenceProfile)
        );
    }

    @Override
    public boolean deleteLayout(
            final LayoutPersistenceProfile layoutPersistenceProfile
    ) throws BentoStateException {

        return selectStorageProvider(layoutPersistenceProfile).deleteLayout(
                layoutPersistenceProfile.layoutIdentifier(),
                codecIdentifier(layoutPersistenceProfile)
        );
    }

    /**
     * {@return the {@link LayoutCodecProvider} the profile selects.}
     *
     * @param layoutPersistenceProfile identifies the codec provider to select.
     * @throws BentoStateException when no single codec provider can be selected.
     */
    private LayoutCodecProvider selectCodec(
            final LayoutPersistenceProfile layoutPersistenceProfile
    ) throws BentoStateException {
        return selectProvider(
                LayoutCodecProvider.class,
                layoutCodecProviders,
                layoutPersistenceProfile.codecIdentifier()
        );
    }

    /**
     * {@return the {@link LayoutStorageProvider} the profile selects.}
     *
     * @param layoutPersistenceProfile identifies the storage provider to select.
     * @throws BentoStateException when no single storage provider can be selected.
     */
    private LayoutStorageProvider selectStorageProvider(
            final LayoutPersistenceProfile layoutPersistenceProfile
    ) throws BentoStateException {
        return selectProvider(
                LayoutStorageProvider.class,
                layoutStorageProviders,
                layoutPersistenceProfile.storageIdentifier()
        );
    }

    /**
     * {@return the identifier of the codec the profile selects.}
     *
     * <p>Read from a codec rather than from its provider, because it is a codec's
     * identifier that a storage destination files a layout under, and nothing
     * requires the two to be the same string.</p>
     *
     * @param layoutPersistenceProfile identifies the codec provider to select.
     * @throws BentoStateException when no single codec provider can be selected.
     */
    private String codecIdentifier(
            final LayoutPersistenceProfile layoutPersistenceProfile
    ) throws BentoStateException {
        return selectCodec(layoutPersistenceProfile)
                .getLayoutCodec()
                .getIdentifier();
    }

    /**
     * Discovers implementations of a service interface.
     *
     * <p>The loader is passed explicitly. The one-argument
     * {@code ServiceLoader.load} is defined as using the <em>thread context</em>
     * class loader, which makes discovery depend on which thread constructed this
     * object - and in a container that leaves it unset, or sets it to a loader
     * that cannot see this module's dependencies, nothing is found. The failure
     * then reads as "no implementation exists" rather than "the loader could not
     * see it". The service interface's own loader is the deterministic choice,
     * because it is the module graph declaring the {@code uses} clause that is
     * meant to resolve the {@code provides}.</p>
     *
     * @param providerType the service interface to discover implementations of.
     * @param <T> the service interface type.
     * @return the discovered implementations.
     */
    private static <T> List<T> loadProviders(final Class<T> providerType) {
        return ServiceLoader.load(providerType, providerType.getClassLoader())
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
            // Naming the loader as a possibility, because an implementation that
            // is present but invisible produces this same empty list, and the
            // message otherwise reads as a definite "it does not exist".
            throw new BentoStateException(
                    "No " + providerType.getSimpleName() + " implementation was found. " +
                            "Add a runtime dependency that provides " + providerType.getSimpleName() + ". " +
                            "If one is present but in a class loader this module cannot see, " +
                            "pass it to the DefaultDockingLayoutPersistenceProvider(List, List) constructor."
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
