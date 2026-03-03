package software.coley.bentofx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.building.ControlsBuilding;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.building.PlaceholderBuilding;
import software.coley.bentofx.building.StageBuilding;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableClickBehavior;
import software.coley.bentofx.dockable.DockableDragDropBehavior;
import software.coley.bentofx.event.DockEvent;
import software.coley.bentofx.event.EventBus;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.search.SearchHandler;

import java.util.Objects;

/**
 * Top level controller for docking operations.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
public class Bento implements Identifiable {

    private final @NotNull String identifier;
	private final @NotNull ObservableList<@NotNull DockContainerRootBranch> rootContainers =
            FXCollections.observableArrayList();
	private final @NotNull ObservableList<@NotNull DockContainerRootBranch> rootContainersView =
            FXCollections.unmodifiableObservableList(rootContainers);
	private final @NotNull EventBus eventBus = newEventBus();
	private final @NotNull SearchHandler searchHandler = newSearchHandler();
	private final @NotNull StageBuilding stageBuilding = newStageBuilding();
	private final @NotNull ControlsBuilding controlsBuilding = newControlsBuilding();
	private final @NotNull DockBuilding dockBuilding = newDockBuilding();
	private final @NotNull PlaceholderBuilding placeholderBuilding = newPlaceholderBuilding();
	private final @NotNull DockableDragDropBehavior dragDropBehavior = newDragDropBehavior();
	private final @NotNull DockableClickBehavior clickBehavior = newClickBehavior();

    public Bento() {
        identifier = DockBuilding.uid("cBento");
    }

    public Bento(final @NotNull String identifier) {
        Objects.requireNonNull(identifier);
        this.identifier = identifier;
    }

	protected @NotNull EventBus newEventBus() {
		return new EventBus();
	}

	protected @NotNull SearchHandler newSearchHandler() {
		return new SearchHandler(this);
	}

	protected @NotNull StageBuilding newStageBuilding() {
		return new StageBuilding(this);
	}

	protected @NotNull ControlsBuilding newControlsBuilding() {
		return new ControlsBuilding();
	}

	protected @NotNull DockBuilding newDockBuilding() {
		return new DockBuilding(this);
	}

	protected @NotNull PlaceholderBuilding newPlaceholderBuilding() {
		return new PlaceholderBuilding();
	}

	protected @NotNull DockableDragDropBehavior newDragDropBehavior() {
		return new DockableDragDropBehavior() {};
	}

	protected @NotNull DockableClickBehavior newClickBehavior() {
		return new DockableClickBehavior() {};
	}

    /**
     * @return the identifier specified when creating this {@code Bento}. This
     * identifier is not guaranteed to be unique.
     */
    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    /**
	 * @return Bus for handling event firing and event listeners.
	 */
	public @NotNull EventBus events() {
		return eventBus;
	}

	/**
	 * @return Search operations.
	 */
	public @NotNull SearchHandler search() {
		return searchHandler;
	}

	/**
	 * @return Builders for {@link DragDropStage}.
	 */
	public @NotNull StageBuilding stageBuilding() {
		return stageBuilding;
	}

	/**
	 * @return Builders for various bento UI controls.
	 */
	public @NotNull ControlsBuilding controlsBuilding() {
		return controlsBuilding;
	}

	/**
	 * @return Builders for {@link DockContainer} and {@link Dockable}.
	 */
	public @NotNull DockBuilding dockBuilding() {
		return dockBuilding;
	}

	/**
	 * @return Builders for placeholder content.
	 */
	public @NotNull PlaceholderBuilding placeholderBuilding() {
		return placeholderBuilding;
	}

	/**
	 * @return Behavior implementation for drag-drop operations.
	 */
	public @NotNull DockableDragDropBehavior getDragDropBehavior() {
		return dragDropBehavior;
	}

	/**
	 * @return Behavior implementation for click operations.
	 */
	public @NotNull DockableClickBehavior getClickBehavior() {
		return clickBehavior;
	}

	/**
	 * @return List of tracked root contents.
	 *
	 * @see #registerRoot(DockContainerRootBranch)
	 * @see #unregisterRoot(DockContainerRootBranch)
	 */
	public @NotNull ObservableList<@NotNull DockContainerRootBranch> getRootContainers() {
		return rootContainersView;
	}

	/**
	 * @param container
	 * 		Root container to register.
	 *
	 * @return {@code true} when registered.
	 */
	public boolean registerRoot(@NotNull DockContainerRootBranch container) {
		if (!rootContainers.contains(container)) {
			rootContainers.add(container);
			eventBus.fire(new DockEvent.RootContainerAdded(container));
			return true;
		}
		return false;
	}

	/**
	 * @param container
	 * 		Root container to unregister.
	 *
	 * @return {@code true} when unregistered.
	 */
	public boolean unregisterRoot(@NotNull DockContainerRootBranch container) {
		if (rootContainers.remove(container)) {
			eventBus.fire(new DockEvent.RootContainerRemoved(container));
			return true;
		}
		return false;
	}
}
