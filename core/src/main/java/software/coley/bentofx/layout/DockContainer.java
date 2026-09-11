package software.coley.bentofx.layout;

import javafx.scene.layout.Region;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.BentoBacked;
import software.coley.bentofx.Identifiable;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.path.DockContainerPath;
import software.coley.bentofx.search.SearchVisitor;

import java.util.Collections;
import java.util.List;

/**
 * Outlines the types of containers housing dockable content.
 *
 * @author Matt Coley
 * @see DockContainerBranch
 * @see DockContainerLeaf
 */
public sealed interface DockContainer extends BentoBacked, Identifiable permits DockContainerBranch, DockContainerLeaf {
	/**
	 * {@return path to this container from the root container that holds this container}
	 */
	default DockContainerPath getPath() {
		DockContainer parent = getParentContainer();
		if (parent != null)
			return parent.getPath().withChild(this);
		return new DockContainerPath(Collections.singletonList(this));
	}

	/**
	 * {@return parent container that holds this container, or {@code null} when this container is a root}
	 */
	@Nullable
	DockContainerBranch getParentContainer();

	/**
	 * Record the given container as this container's parent.
	 * Does not actually mutate the hierarchy and is just for state tracking.
	 *
	 * @param container
	 * 		Container to assign as this container's parent.
	 */
	void setParentContainer(DockContainerBranch container);

	/**
	 * Remove the given container as this container's parent.
	 * Does not actually mutate the hierarchy and is just for state tracking.
	 *
	 * @param parent
	 * 		Container to remove as this container's parent.
	 */
	void removeAsParentContainer(DockContainerBranch parent);

	/**
	 * {@return {@code true} when the visit shall continue}
	 *
	 * @param visitor
	 * 		Visitor to control continued traversal.
	 */
	boolean visit(SearchVisitor visitor);

	/**
	 * {@return unmodifiable list of dockables within this container}
	 */
	List<Dockable> getDockables();

	/**
	 * {@return {@code true} if one or more of the dockables were added}
	 *
	 * @param dockables
	 * 		Dockables to add.
	 */
	default boolean addDockables(Dockable... dockables) {
		boolean changed = false;
		for (Dockable dockable : dockables)
			changed |= addDockable(dockable);
		return changed;
	}

	/**
	 * {@return {@code true} when added}
	 *
	 * @param dockable
	 * 		Dockable to add.
	 */
	boolean addDockable(Dockable dockable);

	/**
	 * {@return {@code true} when added}
	 *
	 * @param dockable
	 * 		Dockable to add.
	 * @param index
	 * 		Index to add the dockable at.
	 */
	boolean addDockable(int index, Dockable dockable);

	/**
	 * {@return {@code true} when removed}
	 *
	 * @param dockable
	 * 		Dockable to remove.
	 */
	boolean removeDockable(Dockable dockable);

	/**
	 * {@return {@code true} when removed}
	 *
	 * @param dockable
	 * 		Dockable to close and then remove.
	 */
	boolean closeDockable(Dockable dockable);

	/**
	 * Remove this container within the {@link #getParentContainer() parent container}.
	 *
	 * @return {@code true} when removed.
	 */
	default boolean removeFromParent() {
		DockContainerBranch parent = getParentContainer();
		if (parent != null)
			return parent.removeContainer(this);
		return false;
	}

	/**
	 * {@return {@code true} to {@link #removeFromParent() prune} when this container has no remaining dockables}
	 */
	boolean doPruneWhenEmpty();

	/**
	 * @param pruneWhenEmpty
	 *        {@code true} to {@link #removeFromParent() prune} when this container has no remaining dockables.
	 */
	void setPruneWhenEmpty(boolean pruneWhenEmpty);

	/**
	 * {@return self, cast to region}
	 */
	default Region asRegion() {
		return (Region) this;
	}
}
