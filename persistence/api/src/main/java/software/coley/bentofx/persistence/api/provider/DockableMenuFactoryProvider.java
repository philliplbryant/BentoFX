package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.dockable.Dockable;
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
	 * Returns the {@link DockableMenuFactory} with the given identifier.
	 *
	 * @param identifier the identifier of the {@link DockableMenuFactory}
	 * to be returned.
	 *
	 * @return the {@link DockableMenuFactory} with the given identifier.
	 */
	Optional<DockableMenuFactory> getDockableMenuFactory(String identifier);
}
