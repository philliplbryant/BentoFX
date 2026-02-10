/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.persistence.api.codec.DockableState;

import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for getting or
 * creating {@link Dockable} instances and other user interface components.
 * <p>
 * <em>
 * Must be initialized using {@link #init(DockBuilding, DockableMenuFactoryProvider)}
 * prior to calling {@link #resolveDockableState(String)}.
 * </em>
 * </p>
 *
 * @author Phil Bryant
 */
public interface DockableStateProvider {

    /**
     * Initializes this {@code DockableStateProvider}.
     *
     * <p>
     * <em>
     * The {@code DockableStateProvider} must be iniatilized to create
     * {@link DockContainer} and {@link Dockable} instances
     * </em>
     * </p>
     *
     * @param builder {@link DockBuilding} to use to create
     *                {@link DockContainer} and {@link Dockable} instances.
     * @param dockableMenuFactoryProvider {@link DockableMenuFactory} to use to create
     * {@code ContextMenu} instances.
     */
    void init(
            final @NotNull DockBuilding builder,
            final @Nullable DockableMenuFactoryProvider dockableMenuFactoryProvider
            );

    /**
     * Returns the {@link Dockable} with the given identifier.
     *
     * @param id the identifier of the {@link Dockable} to be returned.
     * @return the {@link Dockable} with the given identifier.
     */
    @NotNull Optional<@NotNull DockableState> resolveDockableState(String id);
}
