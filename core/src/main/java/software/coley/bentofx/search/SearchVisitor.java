package software.coley.bentofx.search;

import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;

/**
 * Visitor model to traverse {@link DockContainer} hierarchies.
 *
 * @author Matt Coley
 */
public interface SearchVisitor {
	/**
	 * {@return {@code true} to continue visitation}
	 *
	 * @param container
	 * 		Container to visit.
	 */
	default boolean visitBranch(DockContainerBranch container) {
		return true;
	}

	/**
	 * {@return {@code true} to continue visitation}
	 *
	 * @param container
	 * 		Container to visit.
	 */
	default boolean visitLeaf(DockContainerLeaf container) {
		return true;
	}

	/**
	 * {@return {@code true} to continue visitation}
	 *
	 * @param dockable
	 * 		Dockable to visit.
	 */
	default boolean visitDockable(Dockable dockable) {
		return true;
	}
}
