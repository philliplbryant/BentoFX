package software.coley.bentofx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

/**
 * Top level controller for docking operations.
 *
 * @author Matt Coley
 */
public class Bento {
	private final ObservableList<DockContainerRootBranch> rootContainers = FXCollections.observableArrayList();
	private final ObservableList<DockContainerRootBranch> rootContainersView = FXCollections.unmodifiableObservableList(rootContainers);
	private final EventBus eventBus = newEventBus();
	private final SearchHandler searchHandler = newSearchHandler();
	private final StageBuilding stageBuilding = newStageBuilding();
	private final ControlsBuilding controlsBuilding = newControlsBuilding();
	private final DockBuilding dockBuilding = newDockBuilding();
	private final PlaceholderBuilding placeholderBuilding = newPlaceholderBuilding();
	private final DockableDragDropBehavior dragDropBehavior = newDragDropBehavior();
	private final DockableClickBehavior clickBehavior = newClickBehavior();

	protected EventBus newEventBus() {
		return new EventBus();
	}

	protected SearchHandler newSearchHandler() {
		return new SearchHandler(this);
	}

	protected StageBuilding newStageBuilding() {
		return new StageBuilding(this);
	}

	protected ControlsBuilding newControlsBuilding() {
		return new ControlsBuilding();
	}

	protected DockBuilding newDockBuilding() {
		return new DockBuilding(this);
	}

	protected PlaceholderBuilding newPlaceholderBuilding() {
		return new PlaceholderBuilding();
	}

	protected DockableDragDropBehavior newDragDropBehavior() {
		return new DockableDragDropBehavior() {};
	}

	protected DockableClickBehavior newClickBehavior() {
		return new DockableClickBehavior() {};
	}

	/**
	 * {@return bus for handling event firing and event listeners}
	 */
	public EventBus events() {
		return eventBus;
	}

	/**
	 * {@return search operations}
	 */
	public SearchHandler search() {
		return searchHandler;
	}

	/**
	 * {@return builders for {@link DragDropStage}}
	 */
	public StageBuilding stageBuilding() {
		return stageBuilding;
	}

	/**
	 * {@return builders for various bento UI controls}
	 */
	public ControlsBuilding controlsBuilding() {
		return controlsBuilding;
	}

	/**
	 * {@return builders for {@link DockContainer} and {@link Dockable}}
	 */
	public DockBuilding dockBuilding() {
		return dockBuilding;
	}

	/**
	 * {@return builders for placeholder content}
	 */
	public PlaceholderBuilding placeholderBuilding() {
		return placeholderBuilding;
	}

	/**
	 * {@return behavior implementation for drag-drop operations}
	 */
	public DockableDragDropBehavior getDragDropBehavior() {
		return dragDropBehavior;
	}

	/**
	 * {@return behavior implementation for click operations}
	 */
	public DockableClickBehavior getClickBehavior() {
		return clickBehavior;
	}

	/**
	 * @return List of tracked root contents.
	 *
	 * @see #registerRoot(DockContainerRootBranch)
	 * @see #unregisterRoot(DockContainerRootBranch)
	 */
	public ObservableList<DockContainerRootBranch> getRootContainers() {
		return rootContainersView;
	}

	/**
	 * {@return {@code true} when registered}
	 *
	 * @param container
	 * 		Root container to register.
	 */
	public boolean registerRoot(DockContainerRootBranch container) {
		if (!rootContainers.contains(container)) {
			rootContainers.add(container);
			eventBus.fire(new DockEvent.RootContainerAdded(container));
			return true;
		}
		return false;
	}

	/**
	 * {@return {@code true} when unregistered}
	 *
	 * @param container
	 * 		Root container to unregister.
	 */
	public boolean unregisterRoot(DockContainerRootBranch container) {
		if (rootContainers.remove(container)) {
			eventBus.fire(new DockEvent.RootContainerRemoved(container));
			return true;
		}
		return false;
	}
}
