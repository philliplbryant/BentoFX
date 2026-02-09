package software.coley.boxfx.demo.provider;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerLeafMenuFactory;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;

import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockContainerLeafMenuFactoryProvider}.
 *
 * @author Phil Bryant
 */
public class BoxAppDockContainerLeafMenuFactoryProvider
        implements DockContainerLeafMenuFactoryProvider {

    @Override
    public @NotNull Optional<@NotNull DockContainerLeafMenuFactory> createDockContainerLeafMenuFactory(
            final @NotNull String dockContainerLeafIdentifier
    ) {
        return Optional.of(factory);
    }

    private static final DockContainerLeafMenuFactory factory =
            new DockContainerLeafMenuFactory() {

        @Override
        public @NotNull ContextMenu build(
                @NotNull DockContainerLeaf dockContainerLeaf
        ) {

            ContextMenu menu = new ContextMenu();

            for (final Side side : Side.values()) {
                final MenuItem item = new MenuItem(side.name());
                item.setGraphic(
                        new Label(
                                side == dockContainerLeaf.getSide() ?
                                        "✓" :
                                        " "
                        )
                );
                item.setOnAction(e -> dockContainerLeaf.setSide(side));
                menu.getItems().add(item);
            }

            return menu;
        }
    };
}
