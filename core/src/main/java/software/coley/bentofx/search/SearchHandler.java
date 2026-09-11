package software.coley.bentofx.search;

import javafx.scene.input.DragEvent;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.Bento;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.path.DockContainerPath;
import software.coley.bentofx.path.DockablePath;
import software.coley.bentofx.util.DragUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Search operations within a bento instance.
 *
 * @author Matt Coley
 */
public class SearchHandler {
	private final Bento bento;

	/**
	 * @param bento
	 * 		Parent bento instance.
	 */
	public SearchHandler(Bento bento) {
		this.bento = bento;
	}

	/**
	 * {@return {@code true} when replacement was completed}
	 *
	 * @param identifier
	 * 		Some {@link DockContainer#getIdentifier()}.
	 * @param replacement
	 * 		Content to replace the matched container with.
	 */
	public boolean replaceContainer(String identifier, DockContainer replacement) {
		return replaceContainer(identifier, () -> replacement);
	}

	/**
	 * {@return {@code true} when replacement was completed}
	 *
	 * @param identifier
	 * 		Some {@link DockContainer#getIdentifier()}.
	 * @param replacement
	 * 		Supplier of content to replace the matched container with.
	 */
	public boolean replaceContainer(String identifier, Supplier<DockContainer> replacement) {
		DockContainerPath container = container(identifier);
		if (container == null)
			return false;

		DockContainer original = container.tailContainer();
		DockContainerBranch parent = original.getParentContainer();
		if (parent == null)
			return false;

		return parent.replaceContainer(original, replacement.get());
	}

	/**
	 * {@return path to the matched container, if found}
	 *
	 * @param identifier
	 * 		Some {@link DockContainer#getIdentifier()}.
	 */
	@Nullable
	public DockContainerPath container(String identifier) {
		return container(c -> c.getIdentifier().equals(identifier));
	}

	/**
	 * {@return path to the first matched container, if found}
	 *
	 * @param predicate
	 * 		Predicate to match against some container.
	 */
	@Nullable
	public DockContainerPath container(Predicate<DockContainer> predicate) {
		DockContainerVisitor visitor = new DockContainerVisitor(predicate);
		for (DockContainer container : bento.getRootContainers()) {
			if (!container.visit(visitor))
				break;
		}
		DockContainer result = visitor.getMatchedContainer();
		return result == null ? null : result.getPath();
	}

	/**
	 * {@return path to the associated {@link Dockable} if found}
	 *
	 * @param event
	 * 		A drag event that may have a {@link Dockable} associated with it.
	 */
	@Nullable
	public DockablePath dockable(DragEvent event) {
		String identifier = DragUtils.extractIdentifier(event.getDragboard());
		return identifier == null ? null : dockable(identifier);
	}

	/**
	 * {@return path to the matched container, if found}
	 *
	 * @param identifier
	 * 		Some {@link Dockable#getIdentifier()}.
	 */
	@Nullable
	public DockablePath dockable(String identifier) {
		return dockable(d -> d.getIdentifier().equals(identifier));
	}

	/**
	 * {@return path to the first matched dockable, if found}
	 *
	 * @param predicate
	 * 		Predicate to match against some dockable.
	 */
	@Nullable
	public DockablePath dockable(Predicate<Dockable> predicate) {
		DockableVisitor visitor = new DockableVisitor(predicate);
		for (DockContainer container : bento.getRootContainers()) {
			if (!container.visit(visitor))
				break;
		}
		Dockable result = visitor.getMatchedDockable();
		return result == null ? null : result.getPath();
	}

	/**
	 * {@return all found dockable paths in the current bento instance}
	 */
	public List<DockablePath> allDockables() {
		List<DockablePath> paths = new ArrayList<>();
		for (DockContainerRootBranch root : bento.getRootContainers()) {
			paths.addAll(root.getDockables().stream()
					.map(Dockable::getPath)
					.filter(Objects::nonNull) // Sanity check
					.toList());
		}
		return paths;
	}
}
