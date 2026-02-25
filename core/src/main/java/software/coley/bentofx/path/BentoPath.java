package software.coley.bentofx.path;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.layout.DockContainer;

/**
 * Outline of a path to some bento content.
 *
 * @author Matt Coley
 */
public sealed interface BentoPath permits DockContainerPath, DockablePath {
	/**
	 * @return Root container of the path.
	 */
	@NotNull
	DockContainer rootContainer();
}
