/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import javafx.scene.image.Image;
import software.coley.bentofx.dockable.Dockable;

import java.util.Optional;

/**
 * Factory for getting or creating [Dockable] instances and other user interface
 * components.
 */
public interface DockableProvider {

    /**
     * Returns the {@link Dockable} with the given identifier.
     *
     * @param id the identifier of the {@link Dockable} to be returned.
     * @return the {@link Dockable} with the given identifier.
     */
    Optional<Dockable> resolveDockable(String id);

    /**
     * Returns the default {@link Image} to be used for {@code DragDropStage}
     * instances.
     *
     * @return the default {@link Image} to be used for {@code DragDropStage}
     * instances.
     */
    Optional<Image> getDefaultDragDropStageIcon();
}
