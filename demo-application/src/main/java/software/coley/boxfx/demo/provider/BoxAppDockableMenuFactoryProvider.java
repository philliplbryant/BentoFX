package software.coley.boxfx.demo.provider;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;
import software.coley.bentofx.persistence.api.provider.DockableMenuFactoryProvider;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableMenuFactory}.
 *
 * @author Phil Bryant
 */
public class BoxAppDockableMenuFactoryProvider
        implements DockableMenuFactoryProvider {

    @Override
    public @Nullable DockableMenuFactory createDockableMenuFactory(@NotNull Dockable dockable) {
        return d -> new ContextMenu(
                new MenuItem("Menu for : " + dockable.getTitle()),
                new SeparatorMenuItem(),
                new MenuItem("Stuff")
        );
    }
}
