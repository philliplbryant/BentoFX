package software.coley.bentofx.layout.container;

import javafx.scene.Node;

/**
 * Factory to create a {@link Node} placeholder display for some given {@link DockContainerLeaf}.
 * Implementations should create <b>NEW</b> instances for <b>EACH</b> call.
 *
 * @author Matt Coley
 */
public interface DockContainerLeafPlaceholderFactory {
	/**
	 * {@return placeholder for the container}
	 *
	 * @param container
	 * 		Container to create a placeholder display for.
	 */
	Node build(DockContainerLeaf container);
}
