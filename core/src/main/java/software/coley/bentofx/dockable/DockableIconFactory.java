package software.coley.bentofx.dockable;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

/**
 * Factory to create a {@link Node} graphic for some given {@link Dockable}.
 * Implementations should create <b>NEW</b> instances for <b>EACH</b> call.
 *
 * @author Matt Coley
 */
public interface DockableIconFactory {
	/**
	 * {@return graphic for the dockable}
	 *
	 * @param dockable
	 * 		Dockable to create a graphic for.
	 */
	@Nullable
	Node build(Dockable dockable);
}
