package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.dockable.DockableMenuFactory;

import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for resolving
 * {@link DockableMenuFactory} instances.
 *
 * @author Phil Bryant
 */
public interface DockableMenuFactoryProvider {

	/**
     * {@return an {@link Optional<DockableMenuFactory>} with a
     * {@link DockableMenuFactory} the given identifier.} Implementations should
     * return an empty {@link Optional<DockableMenuFactory>} when no context
     * menu factory is available for the identifier.
     *
     * @param identifier the identifier of the {@link DockableMenuFactory}
     * to be returned.
	 */
	Optional<DockableMenuFactory> getDockableMenuFactory(String identifier);
}
