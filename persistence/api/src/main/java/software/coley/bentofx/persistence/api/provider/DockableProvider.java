package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.building.DockBuilding;

public interface DockableProvider {

    @NotNull software.coley.bentofx.persistence.api.DockableProvider createDockableResolver(
            @NotNull final DockBuilding dockBuilding
    );
}
