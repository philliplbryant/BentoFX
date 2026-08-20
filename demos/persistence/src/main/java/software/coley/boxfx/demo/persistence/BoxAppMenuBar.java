package software.coley.boxfx.demo.persistence;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import software.coley.bentofx.persistence.core.api.provider.DockingLayoutRestorable;
import software.coley.bentofx.persistence.core.ui.LayoutsMenu;

/**
 * The {@link MenuBar} for {@link BoxApp}.
 *
 * <p>Contains {@code File} and {@code Window} menus. The {@code File} menu
 * allows users to exit the application. The {@code Window} menu provides a
 * {@link LayoutsMenu}, which allows users to switch between the default layout
 * and layouts a user has named, and save, rename, and delete saved layouts.</p>
 *
 * <p>All this class does is assemble. Exiting is the one thing here that
 * belongs to this application; everything about layouts is
 * {@code LayoutsMenu}'s.</p>
 *
 * @author Phil Bryant
 */
final class BoxAppMenuBar extends MenuBar {

	/**
	 * @param owner the window the dialogs these menus raise belong to.
	 * @param dockingLayoutRestorable the application whose docking layout the
	 * {@code Layouts} menu switches.
	 * @param onExit what the {@code File | Exit} item runs.
	 */
	BoxAppMenuBar(
			final Window owner,
			final DockingLayoutRestorable dockingLayoutRestorable,
			final Runnable onExit
	) {
		getMenus().setAll(
				createFileMenu(onExit),
				createWindowMenu(
						new LayoutsMenu(owner, dockingLayoutRestorable)
				)
		);
	}

	/**
	 * {@return the {@code File} menu.}
	 *
	 * @param onExit what the {@code Exit} item runs.
	 */
	private static Menu createFileMenu(final Runnable onExit) {
		final MenuItem exitItem = new MenuItem("E_xit");
		exitItem.setOnAction(event -> onExit.run());

		final Menu fileMenu = new Menu("_File");
		fileMenu.getItems().add(exitItem);
		return fileMenu;
	}

	/**
	 * {@return the {@code Window} menu.}
	 *
	 * @param layoutsMenu the {@code Layouts} menu to hold.
	 */
	private static Menu createWindowMenu(final LayoutsMenu layoutsMenu) {
		final Menu windowMenu = new Menu("_Window");
		windowMenu.getItems().add(layoutsMenu);
		return windowMenu;
	}
}
