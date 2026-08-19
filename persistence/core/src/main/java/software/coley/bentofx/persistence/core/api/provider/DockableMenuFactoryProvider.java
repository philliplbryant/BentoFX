package software.coley.bentofx.persistence.core.api.provider;

import software.coley.bentofx.dockable.DockableMenuFactory;

import java.util.Optional;

/**
 * Resolves {@link DockableMenuFactory} instances by identifier.
 *
 * <p>Offered for applications to implement; nothing in the persistence framework
 * consumes it. Unlike its siblings here it is not even a parameter - no
 * {@link DockingLayoutPersistenceProvider} method accepts one. It exists so that a
 * {@link DockableStateProvider} implementation has a ready-made shape for looking
 * up the menu factory to attach to a {@code Dockable} it rebuilds. If the
 * application does not call it, nothing will.</p>
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
