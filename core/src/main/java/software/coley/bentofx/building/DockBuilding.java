package software.coley.bentofx.building;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Bento;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;

import java.util.Random;

/**
 * Builders for {@link DockContainer} and {@link Dockable} instances.
 *
 * @author Matt Coley
 */
public class DockBuilding {
	private static final Random RANDOM = new Random();
	private final Bento bento;

	/**
	 * @param bento
	 * 		Parent bento instance.
	 */
	public DockBuilding(@NotNull Bento bento) {
		this.bento = bento;
	}

	/**
	 * @return New dockable.
	 */
	@NotNull
	public Dockable dockable() {
		return dockable(uid("cDockable"));
	}

	/**
	 * @param identifier
	 * 		Identifier to assign to the created dockable.
	 *
	 * @return New dockable.
	 */
	@NotNull
	public Dockable dockable(@NotNull String identifier) {
		return new Dockable(bento, identifier);
	}

	/**
	 * @return New root container.
	 *
	 * @see Bento#registerRoot(DockContainerRootBranch)
	 * @see Bento#unregisterRoot(DockContainerRootBranch)
	 */
	@NotNull
	public DockContainerRootBranch root() {
		return root(uid("cRoot"));
	}


	/**
	 * @param identifier
	 * 		Identifier to assign to the created container.
	 *
	 * @return New root container.
	 *
	 * @see Bento#registerRoot(DockContainerRootBranch)
	 * @see Bento#unregisterRoot(DockContainerRootBranch)
	 */
	@NotNull
	public DockContainerRootBranch root(@NotNull String identifier) {
		return new DockContainerRootBranch(bento, identifier);
	}

	/**
	 * @return New branch container.
	 */
	@NotNull
	public DockContainerBranch branch() {
		return branch(uid("cBranch"));
	}

	/**
	 * @param identifier
	 * 		Identifier to assign to the created container.
	 *
	 * @return New branch container.
	 */
	@NotNull
	public DockContainerBranch branch(@NotNull String identifier) {
		return new DockContainerBranch(bento, identifier);
	}

	/**
	 * @return New leaf container.
	 */
	@NotNull
	public DockContainerLeaf leaf() {
		return leaf(uid("cLeaf"));
	}

	/**
	 * @param identifier
	 * 		Identifier to assign to the created container.
	 *
	 * @return New branch container.
	 */
	@NotNull
	public DockContainerLeaf leaf(@NotNull String identifier) {
		return new DockContainerLeaf(bento, identifier);
	}

	@NotNull
	public static String uid(@NotNull String prefix) {
		StringBuilder suffix = new StringBuilder(8);
		for (int i = 0; i < 8; i++)
			suffix.append((char) RANDOM.nextInt('A', 'Z'));
		return prefix + ":" + suffix;
	}
}
