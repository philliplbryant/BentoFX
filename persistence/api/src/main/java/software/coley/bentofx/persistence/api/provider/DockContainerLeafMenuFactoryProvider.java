package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.layout.container.DockContainerLeaf;
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
     * Creates a {@link DockContainerLeafMenuFactory} for the {@link DockContainerLeaf}
     * with the specified identifier.
     *
     * @return a {@link DockContainerLeafMenuFactory}.
     */
    Optional<DockContainerLeafMenuFactory> getDockContainerLeafMenuFactory(
            final String dockContainerLeafIdentifier
    );
}
