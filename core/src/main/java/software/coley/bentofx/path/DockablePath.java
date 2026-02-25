package software.coley.bentofx.path;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerLeaf;

import java.util.List;

/**
 * Path to a given dockable, starting from the root container <i>(Top {@link DockContainer#getParentContainer()} value)</i>
 * all the way down to the container that holds the target dockable.
 *
 * @param containers
 * 		Containers up to and including some dockable's parent container.
 * @param dockable
 * 		Target dockable.
 */
public record DockablePath(@NotNull List<DockContainer> containers, @NotNull Dockable dockable) implements BentoPath {
	@NotNull
	@Override
	public DockContainer rootContainer() {
		// There must always be at least one container in a path since a dockable needs a parent to be placed into.
		return containers.getFirst();
	}

	@NotNull
	public DockContainerLeaf leafContainer() {
		// A dockable can only be put in a leaf, so this should be a safe cast.
		return (DockContainerLeaf) containers.getLast();
	}
}
