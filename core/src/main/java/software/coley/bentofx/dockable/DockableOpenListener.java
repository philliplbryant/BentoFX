package software.coley.bentofx.dockable;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.path.DockablePath;

/**
 * Listener that is invoked when a {@link Dockable} is added to a {@link DockContainer}.
 *
 * @author Matt Coley
 */
public interface DockableOpenListener {
	/**
	 * @param path
	 * 		Path to opened dockable.
	 * @param dockable
	 * 		Closed dockable.
	 */
	void onOpen(@NotNull DockablePath path, @NotNull Dockable dockable);
}
