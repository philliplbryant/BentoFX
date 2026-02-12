package software.coley.boxfx.demo.provider;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableMenuFactory}.
 *
 * @author Phil Bryant
 */
public class BoxAppDockableMenuFactory implements DockableMenuFactory {

    @Override
    public @NotNull ContextMenu build(@NotNull Dockable dockable) {
        return new ContextMenu(
                new MenuItem("Menu for : " + dockable.getTitle()),
                new SeparatorMenuItem(),
                new MenuItem("Stuff")
        );
    }
}
