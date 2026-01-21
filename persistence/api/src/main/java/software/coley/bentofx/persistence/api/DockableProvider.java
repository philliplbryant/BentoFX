/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;

import java.util.Collection;
import java.util.Optional;

/**
 * Factory for getting or creating [Dockable] instances and other user interface
 * components.
 */
public interface DockableProvider {
// TODO BENTO-13: Rename class to DockableResolver
    /**
     * Initializes this {@code DockableProvider}.
     *
     * <p>
     * <em>
     * The {@code DockableProvider} must be iniatilized to create
     * {@link DockContainer} and {@link Dockable} instances
     * </em>
     * </p>
     *
     * @param builder {@link DockBuilding} to use to create
     *                {@link DockContainer} and {@link Dockable} instances.
     */
    void init(final @NotNull DockBuilding builder);

    /**
     * Returns the {@link Dockable} with the given identifier.
     *
     * @param id the identifier of the {@link Dockable} to be returned.
     * @return the {@link Dockable} with the given identifier.
     */
    Optional<@Nullable Dockable> resolveDockable(String id);

    /**
     * Returns a collection of varying sizes for the default {@link Image} to be
     * used for {@code Stage} and {@code DragDropStage} instances.
     *
     * @return a collection of varying sizes for the default {@link Image} to be
     * used for {@code Stage} and {@code DragDropStage} instances.
     */
    @NotNull Collection<@NotNull Image> getDefaultStageIcons();
}
