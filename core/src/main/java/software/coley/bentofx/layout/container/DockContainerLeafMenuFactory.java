package software.coley.bentofx.layout.container;

import javafx.scene.control.ContextMenu;
import org.jspecify.annotations.Nullable;

/**
 * Factory to create a {@link ContextMenu} for some given {@link DockContainerLeaf}.
 *
 * @author Matt Coley
 */
public interface DockContainerLeafMenuFactory {
	/**
	 * {@return context menu for the container}
	 *
	 * @param container
	 * 		Container to create a context menu for.
	 */
	@Nullable
	ContextMenu build(DockContainerLeaf container);
}
