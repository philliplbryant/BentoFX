package software.coley.bentofx.persistence.api.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutSaver} and {@link LayoutRestorer} implementations.
 *
 * @author Phil Bryant
 */
public interface DockingLayoutPersistenceProvider {

    LayoutSaver getLayoutSaver(
            final String layoutIdentifier,
            final BentoProvider bentoProvider
    );

    LayoutRestorer getLayoutRestorer(
            final String layoutIdentifier,
            final BentoProvider bentoprovider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    );
}
