package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.layout.container.DockContainerLeafMenuFactory;

import java.util.Optional;


/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@code DockContainerLeafMenuFactory} implementations.
 *
 * @author Phil Bryant
 */
public interface DockContainerLeafMenuFactoryProvider {

    /**
     * {@return an {@link Optional} containing the
     * {@link DockContainerLeafMenuFactory} for the identifier, an empty
     * {@code Optional} when no {@link DockContainerLeafMenuFactory} is
     * available for the identifier.}
     */
    Optional<DockContainerLeafMenuFactory> getDockContainerLeafMenuFactory(
            final String dockContainerLeafIdentifier
    );
}
