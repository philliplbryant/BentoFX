package software.coley.boxfx.demo.provider;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;
import software.coley.bentofx.persistence.api.provider.DockableMenuFactoryProvider;

import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableMenuFactory}.
 *
 * @author Phil Bryant
 */
public class BoxAppDockableMenuFactoryProvider
        implements DockableMenuFactoryProvider {

    @Override
    public @NotNull Optional<DockableMenuFactory> createDockableMenuFactory(
            @NotNull String dockableIdentifier
    ) {
        return Optional.of(factory);
    }

    private static final DockableMenuFactory factory =
            new DockableMenuFactory() {

                @Override
                public @NotNull ContextMenu build(@NotNull Dockable dockable) {
                    return new ContextMenu(
                            new MenuItem("Menu for : " + dockable.getTitle()),
                            new SeparatorMenuItem(),
                            new MenuItem("Stuff")
                    );
                }
            };
}
