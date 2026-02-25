package software.coley.bentofx.search;

import org.jetbrains.annotations.NotNull;
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
	 * @param container
	 * 		Container to visit.
	 *
	 * @return {@code true} to continue visitation.
	 */
	default boolean visitBranch(@NotNull DockContainerBranch container) {
		return true;
	}

	/**
	 * @param container
	 * 		Container to visit.
	 *
	 * @return {@code true} to continue visitation.
	 */
	default boolean visitLeaf(@NotNull DockContainerLeaf container) {
		return true;
	}

	/**
	 * @param dockable
	 * 		Dockable to visit.
	 *
	 * @return {@code true} to continue visitation.
	 */
	default boolean visitDockable(@NotNull Dockable dockable) {
		return true;
	}
}
