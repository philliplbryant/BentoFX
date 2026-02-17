package software.coley.bentofx;

import jakarta.annotation.Nonnull;
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
public class Bento implements Identifiable {

    private final @Nonnull String identifier;
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

    public Bento() {
        identifier = DockBuilding.uid("bento");
    }

    public Bento(final @Nonnull String identifier) {
        this.identifier = identifier;
    }

	@Nonnull
	protected EventBus newEventBus() {
		return new EventBus();
	}

	@Nonnull
	protected SearchHandler newSearchHandler() {
		return new SearchHandler(this);
	}

	@Nonnull
	protected StageBuilding newStageBuilding() {
		return new StageBuilding(this);
	}

	@Nonnull
	protected ControlsBuilding newControlsBuilding() {
		return new ControlsBuilding();
	}

	@Nonnull
	protected DockBuilding newDockBuilding() {
		return new DockBuilding(this);
	}

	@Nonnull
	protected PlaceholderBuilding newPlaceholderBuilding() {
		return new PlaceholderBuilding();
	}

	@Nonnull
	protected DockableDragDropBehavior newDragDropBehavior() {
		return new DockableDragDropBehavior() {};
	}

	@Nonnull
	protected DockableClickBehavior newClickBehavior() {
		return new DockableClickBehavior() {};
	}

    /**
     * @return the identifier specified when creating this {@code Bento}. This
     * identifier is not guaranteed to be unique.
     */
    @Override
    @Nonnull
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean matchesIdentity(@Nonnull Identifiable other) {
        return this.identifier.equals(other.getIdentifier());
    }

    /**
	 * @return Bus for handling event firing and event listeners.
	 */
	@Nonnull
	public EventBus events() {
		return eventBus;
	}

	/**
	 * @return Search operations.
	 */
	@Nonnull
	public SearchHandler search() {
		return searchHandler;
	}

	/**
	 * @return Builders for {@link DragDropStage}.
	 */
	@Nonnull
	public StageBuilding stageBuilding() {
		return stageBuilding;
	}

	/**
	 * @return Builders for various bento UI controls.
	 */
	@Nonnull
	public ControlsBuilding controlsBuilding() {
		return controlsBuilding;
	}

	/**
	 * @return Builders for {@link DockContainer} and {@link Dockable}.
	 */
	@Nonnull
	public DockBuilding dockBuilding() {
		return dockBuilding;
	}

	/**
	 * @return Builders for placeholder content.
	 */
	@Nonnull
	public PlaceholderBuilding placeholderBuilding() {
		return placeholderBuilding;
	}

	/**
	 * @return Behavior implementation for drag-drop operations.
	 */
	@Nonnull
	public DockableDragDropBehavior getDragDropBehavior() {
		return dragDropBehavior;
	}

	/**
	 * @return Behavior implementation for click operations.
	 */
	@Nonnull
	public DockableClickBehavior getClickBehavior() {
		return clickBehavior;
	}

	/**
	 * @return List of tracked root contents.
	 *
	 * @see #registerRoot(DockContainerRootBranch)
	 * @see #unregisterRoot(DockContainerRootBranch)
	 */
	@Nonnull
	public ObservableList<DockContainerRootBranch> getRootContainers() {
		return rootContainersView;
	}

	/**
	 * @param container
	 * 		Root container to register.
	 *
	 * @return {@code true} when registered.
	 */
	public boolean registerRoot(@Nonnull DockContainerRootBranch container) {
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
	public boolean unregisterRoot(@Nonnull DockContainerRootBranch container) {
		if (rootContainers.remove(container)) {
			eventBus.fire(new DockEvent.RootContainerRemoved(container));
			return true;
		}
		return false;
	}
}
