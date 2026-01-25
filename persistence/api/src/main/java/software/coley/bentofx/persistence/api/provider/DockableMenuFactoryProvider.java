package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@code DockableMenuFactory} implementations.
 *
 * @author Phil Bryant
 */
public interface DockableMenuFactoryProvider {

    /**
     * Creates a {@link DockableMenuFactory}.
     *
     * @return a {@link DockableMenuFactory}
     */
    @Nullable DockableMenuFactory createDockableMenuFactory(
            final @NotNull Dockable dockable
    );
}
