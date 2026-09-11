package software.coley.bentofx.dockable;

import javafx.scene.Node;

/**
 * Factory to create a {@link Node} placeholder display for some given {@link Dockable}.
 * Implementations should create <b>NEW</b> instances for <b>EACH</b> call.
 *
 * @author Matt Coley
 */
public interface DockablePlaceholderFactory {
	/**
	 * {@return placeholder for the dockable}
	 *
	 * @param dockable
	 * 		Dockable to create a placeholder display for.
	 */
	Node build(Dockable dockable);
}
