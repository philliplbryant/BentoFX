package software.coley.boxfx.demo.provider;

import javafx.scene.image.Image;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.api.DockableProvider;

import java.util.Optional;

public class BoxAppDockableProvider implements DockableProvider {

    @Override
    public Optional<Dockable> resolveDockable(String id) {
        return Optional.empty();
    }

    @Override
    public Optional<Image> getDefaultDragDropStageIcon() {
        return Optional.empty();
    }
}
