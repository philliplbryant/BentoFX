package software.coley.bentofx.building;

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
	public DockBuilding(Bento bento) {
		this.bento = bento;
	}

	/**
	 * {@return new dockable}
	 */
	public Dockable dockable() {
		return dockable(uid("dockable"));
	}

	/**
	 * {@return new dockable}
	 *
	 * @param identifier
	 * 		Identifier to assign to the created dockable.
	 */
	public Dockable dockable(String identifier) {
		return new Dockable(bento, identifier);
	}

	/**
	 * @return New root container.
	 *
	 * @see Bento#registerRoot(DockContainerRootBranch)
	 * @see Bento#unregisterRoot(DockContainerRootBranch)
	 */
	public DockContainerRootBranch root() {
		return root(uid("croot"));
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
	public DockContainerRootBranch root(String identifier) {
		return new DockContainerRootBranch(bento, identifier);
	}

	/**
	 * {@return new branch container}
	 */
	public DockContainerBranch branch() {
		return branch(uid("cbranch"));
	}

	/**
	 * {@return new branch container}
	 *
	 * @param identifier
	 * 		Identifier to assign to the created container.
	 */
	public DockContainerBranch branch(String identifier) {
		return new DockContainerBranch(bento, identifier);
	}

	/**
	 * {@return new leaf container}
	 */
	public DockContainerLeaf leaf() {
		return leaf(uid("cleaf"));
	}

	/**
	 * {@return new leaf container}
	 *
	 * @param identifier
	 * 		Identifier to assign to the created container.
	 */
	public DockContainerLeaf leaf(String identifier) {
		return new DockContainerLeaf(bento, identifier);
	}

	private static String uid(String prefix) {
		StringBuilder suffix = new StringBuilder(8);
		for (int i = 0; i < 8; i++)
			suffix.append((char) RANDOM.nextInt('A', 'Z'));
		return prefix + ":" + suffix;
	}
}
