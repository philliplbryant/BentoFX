package software.coley.bentofx.control;

import jakarta.annotation.Nonnull;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.building.StageBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;

import java.lang.ref.WeakReference;
import java.util.List;

import static javafx.stage.WindowEvent.*;

/**
 * Stage subtype created by {@link StageBuilding#newStageForDockable(Scene, DockContainerRootBranch, DockContainerLeaf, Dockable, double, double)}
 *
 * @author Matt Coley
 */
public class DragDropStage extends IdentifiableStage {

    private final boolean autoCloseWhenEmpty;
    private WeakReference<Parent> content;

    /**
     * @param autoCloseWhenEmpty Flag to determine if this stage should auto-close if its sole content is removed.
     *                           See	{@link #isAutoCloseWhenEmpty()} for more details.
     */
    public DragDropStage(boolean autoCloseWhenEmpty) {
        this(
                DockBuilding.uid("cDragDropStage"),
                autoCloseWhenEmpty
        );
    }

    /**
     * @param identifier         This objects identifier.
     * @param autoCloseWhenEmpty Flag to determine if this stage should auto-close if its sole content is removed.
     * @see #isAutoCloseWhenEmpty for more details.
     */
    public DragDropStage(
            @Nonnull String identifier,
            boolean autoCloseWhenEmpty
    ) {
        super(identifier);
        this.autoCloseWhenEmpty = autoCloseWhenEmpty;

        addEventFilter(
                WINDOW_CLOSE_REQUEST,
                this::onWindowClose
        );

        addEventFilter(
                WINDOW_HIDDEN,
                this::onWindowHidden
        );

        addEventFilter(
                WINDOW_SHOWN,
                this::onWindowShown
        );
    }

    /**
     * <b>Context:</b>These stages are created when a user drags a {@link Header} into empty space.
     * <p/>
     * Most of the time, if a user drags the {@link Header} from this stage into some other place in another stage,
     * leaving this stage with nothing in {@link DockContainer} it would be ideal to automatically
     * close this window.
     * <p/>
     * When this is {@code true} we do just that.
     *
     * @return {@code true} when this stage should auto-close if its {@link DockContainer} is cleared/removed.
     */
    public boolean isAutoCloseWhenEmpty() {
        return autoCloseWhenEmpty;
    }

    /**
     * Cancel closure if headers that are not closable exist.
     */
    private void onWindowClose(final WindowEvent e) {
        Parent root = getScene().getRoot();
        if (root instanceof DockContainerBranch rootBranch) {
            boolean canClose = true;

            // Try to close all dockables and track if any remain.
            List<Dockable> dockables = rootBranch.getDockables();
            for (Dockable dockable : dockables) {
                DockContainer container = dockable.getContainer();
                if (container != null && !container.closeDockable(dockable))
                    canClose = false;
            }

            // If some headers remain, abort the close.
            if (!canClose)
                e.consume();
        }
    }

    /**
     * Add event filters to clear/restore the scene contents when hiding/showing.
     * Each respective action will trigger the root contents listeners that
     * handle registering/unregistering.
     */
    private void onWindowHidden(final WindowEvent e) {
        Parent parent = getScene().getRoot();
        if (parent != null)
            content = new WeakReference<>(parent);
        getScene().setRoot(new Region());
    }

    private void onWindowShown(final WindowEvent e) {
        if (content != null) {
            getScene().setRoot(content.get());
            content.clear();
            content = null;
        }
    }
}
