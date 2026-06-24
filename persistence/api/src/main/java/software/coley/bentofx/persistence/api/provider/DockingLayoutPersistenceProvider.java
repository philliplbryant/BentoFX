package software.coley.bentofx.persistence.api.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public interface DockingLayoutPersistenceProvider {

    default LayoutSaver getLayoutSaver(
            final String layoutIdentifier,
            final BentoProvider bentoProvider
    ) throws BentoStateException {
        return getLayoutSaver(
                LayoutPersistenceProfile.of(layoutIdentifier),
                bentoProvider
        );
    }

    LayoutSaver getLayoutSaver(
            final LayoutPersistenceProfile layoutPersistenceProfile,
            final BentoProvider bentoProvider
    ) throws BentoStateException;

    default LayoutRestorer getLayoutRestorer(
            final String layoutIdentifier,
            final BentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) throws BentoStateException {
        return getLayoutRestorer(
                LayoutPersistenceProfile.of(layoutIdentifier),
                bentoProvider,
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider
        );
    }

    LayoutRestorer getLayoutRestorer(
            final LayoutPersistenceProfile layoutPersistenceProfile,
            final BentoProvider bentoprovider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) throws BentoStateException;
}
