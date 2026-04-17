package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.impl.codec.DockableState;

import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for getting or
 * creating {@link Dockable} instances and other user interface components.
 *
 * @author Phil Bryant
 */
public interface DockableStateProvider {

    /**
     * Returns the {@link Dockable} with the given identifier.
     *
     * @param id the identifier of the {@link Dockable} to be returned.
     * @return the {@link Dockable} with the given identifier.
     */
    Optional<DockableState> resolveDockableState(String id);
}
