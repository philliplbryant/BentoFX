package software.coley.boxfx.demo.persistence.provider;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;
import software.coley.bentofx.persistence.api.provider.DockableMenuFactoryProvider;

import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableMenuFactoryProvider} that returns the same, sample menu for
 * all {@link Dockable}.
 */
public class BoxAppDockableMenuFactoryProvider implements DockableMenuFactoryProvider {

	@Override
	public Optional<DockableMenuFactory> getDockableMenuFactory(
			final String dockContainerLeafIdentifier
	) {
		return Optional.of(factory);
	}

	private static final DockableMenuFactory factory = dockable ->
			new ContextMenu(
					new MenuItem("Menu for : " + dockable.getTitle()),
					new SeparatorMenuItem(),
					new MenuItem("Stuff")
			);
}
