package software.coley.boxfx.demo.persistence;

import software.coley.bentofx.persistence.api.DockingLayout;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;

import java.util.function.Supplier;

/**
 * An application whose docking layout can be replaced with another.
 *
 * <p>The three methods belong together because both of the layout-returning
 * ones exist to be handed to {@link #switchToLayout(Supplier)}: one for the
 * layout the application builds for itself, one for a layout in storage.</p>
 *
 * <p>A switch takes a {@link Supplier} rather than a {@link DockingLayout}
 * because reading the layout is part of the switch, not something done before
 * it. An implementation has to stop whatever is saving the arrangement on
 * screen before anything reads a replacement, so it is the implementation that
 * decides when the supplier runs.</p>
 *
 * @author Phil Bryant
 */
public interface DockingLayoutRestorable {

	/**
	 * {@return the layout the application builds for itself.}
	 *
	 * <p>What a restore falls back to when nothing has been saved, and what the
	 * {@code Default} menu item switches to.</p>
	 */
	DockingLayout getDefaultDockingLayout();

	/**
	 * {@return the identified {@link DockingLayout} when one has been saved, or
	 * the fallback supplier's layout when none has or it cannot be read.}
	 *
	 * @param layoutPersistenceProfile identifies the layout to restore.
	 * @param fallbackLayoutSupplier supplies the layout to use instead when
	 * nothing can be restored. Startup passes the default layout; a switch
	 * between saved layouts passes an empty one, so that a failure shows up as
	 * nothing to apply rather than as the default layout arriving unasked.
	 *
	 * @see #getDefaultDockingLayout()
	 */
	DockingLayout getDockingLayout(
			LayoutPersistenceProfile layoutPersistenceProfile,
			Supplier<DockingLayout> fallbackLayoutSupplier
	);

	/**
	 * {@return {@code true} when the supplied layout was applied; otherwise,
	 * {@code false}, meaning the layout on screen was left alone.}
	 *
	 * <p>Telling the user about a switch that did not happen is left to the
	 * caller, which is where the wording for a restore that could not be
	 * carried out belongs.</p>
	 *
	 * @param dockingLayoutSupplier supplies the layout to switch to.
	 */
	boolean switchToLayout(Supplier<DockingLayout> dockingLayoutSupplier);
}
