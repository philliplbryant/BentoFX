package software.coley.bentofx.persistence.api;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.control.IdentifiableStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.api.codec.DockableState;

public abstract class PersistableStage extends IdentifiableStage {

    public PersistableStage(final @NotNull String identifier) {
        super(identifier);
    }
    public abstract @NotNull IdentifiableStageLayout getLayout();

    public abstract @NotNull Bento getBento();

    public static @NotNull Dockable createDockable(
            final @NotNull Bento bento,
            final @NotNull DockableState dockableState
    ) {
        final DockBuilding dockBuilding = bento.dockBuilding();

        final Dockable dockable =
                dockBuilding.dockable(dockableState.getIdentifier());

        dockableState.getDockableNode().ifPresent(
                dockable::setNode
        );

        dockableState.getTitle().ifPresent(
                dockable::setTitle
        );

        dockableState.getDockableIconFactory().ifPresent(
                dockable::setIconFactory
        );

        dockableState.getDockableMenuFactory().ifPresent(
                dockable::setContextMenuFactory
        );

        return dockable;
    }
}
