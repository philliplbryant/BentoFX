package software.coley.boxfx.demo.persistence.provider;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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
    public Optional<DockContainerLeafMenuFactory> createDockContainerLeafMenuFactory(
            final String dockContainerLeafIdentifier
    ) {
        return Optional.of(factory);
    }

    private static final DockContainerLeafMenuFactory factory =
            dockContainerLeaf -> {

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
            };
}
