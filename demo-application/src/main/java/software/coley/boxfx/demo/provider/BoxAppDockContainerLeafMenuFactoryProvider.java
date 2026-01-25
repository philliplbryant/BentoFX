package software.coley.boxfx.demo.provider;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerLeafMenuFactory;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;

public class BoxAppDockContainerLeafMenuFactoryProvider
        implements DockContainerLeafMenuFactoryProvider {

    @Override
    public @Nullable DockContainerLeafMenuFactory createDockContainerLeafMenuFactory(
            final @NotNull DockContainerLeaf dockContainerLeaf
    ) {
        return d ->
                addSideOptions(new ContextMenu(), dockContainerLeaf);
    }

    @NotNull
    private static ContextMenu addSideOptions(
            @NotNull ContextMenu menu,
            @NotNull DockContainerLeaf space
    ) {
        for (final Side side : Side.values()) {
            final MenuItem item = new MenuItem(side.name());
            item.setGraphic(new Label(side == space.getSide() ? "✓" : " "));
            item.setOnAction(e -> space.setSide(side));
            menu.getItems().add(item);
        }
        return menu;
    }
}
