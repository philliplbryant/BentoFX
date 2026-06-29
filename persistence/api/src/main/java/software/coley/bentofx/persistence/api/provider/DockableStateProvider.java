package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.api.state.DockableState;

import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for getting or
 * creating {@link DockableState} instances and other user interface components.
 *
 * @author Phil Bryant
 */
public interface DockableStateProvider {

    /**
     * {@return an {@link Optional} containing the {@link DockableState} with the
     * given identifier, {@code null} when no {@link DockableState} with the
     * identifier can be found.}
     * @param id the identifier of the {@link Dockable} to be returned.
     */
    Optional<DockableState> resolveDockableState(String id);
}
