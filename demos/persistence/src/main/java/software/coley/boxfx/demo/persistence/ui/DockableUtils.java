package software.coley.boxfx.demo.persistence.ui;

import javafx.scene.control.Tooltip;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.impl.codec.DockableState;

/**
 * Utility class for creating {@link Dockable} from {@link DockableState}.
 *
 * @author Phil Bryant
 */
public class DockableUtils {
    private DockableUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Dockable createDockable(
            final Bento bento,
            final DockableState dockableState
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

        dockableState.getTooltip().ifPresent( tooltipText ->
                dockable.setTooltip(new Tooltip(tooltipText))
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
