package software.coley.bentofx.dockable;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.path.DockablePath;

/**
 * Listener that is invoked when a {@link Dockable} is moved to a new {@link DockContainer}.
 *
 * @author Matt Coley
 */
public interface DockableMoveListener {
	/**
	 * @param oldPath
	 * 		Path to old dockable location.
	 * @param newPath
	 * 		Path to new dockable location.
	 * @param dockable
	 * 		Moved dockable.
	 */
	void onMove(@NotNull DockablePath oldPath, @NotNull DockablePath newPath, @NotNull Dockable dockable);
}
