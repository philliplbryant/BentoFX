/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.api.codec.DockableState;

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
    @NotNull Optional<@NotNull DockableState> resolveDockableState(String id);
}
