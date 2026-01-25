package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.dockable.DockableMenuFactory;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerLeafMenuFactory;



/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@code DockContainerLeafMenuFactory} implementations.
 *
 * @author Phil Bryant
 */
public interface DockContainerLeafMenuFactoryProvider {

    /**
     * Creates a {@link DockableMenuFactory}.
     *
     * @return a {@link DockableMenuFactory}
     */
    @Nullable DockContainerLeafMenuFactory createDockContainerLeafMenuFactory(
            final @NotNull DockContainerLeaf dockContainerLeaf
    );
}
