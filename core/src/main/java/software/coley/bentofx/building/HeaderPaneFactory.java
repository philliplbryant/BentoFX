package software.coley.bentofx.building;

import software.coley.bentofx.control.HeaderPane;
import software.coley.bentofx.layout.container.DockContainerLeaf;

/**
 * Factory for building a {@link HeaderPane} for a {@link DockContainerLeaf}.
 *
 * @author Matt Coley
 */
public interface HeaderPaneFactory {
	/**
	 * {@return new header pane}
	 *
	 * @param container
	 * 		Parent container.
	 */
	HeaderPane newHeaderPane(DockContainerLeaf container);
}
