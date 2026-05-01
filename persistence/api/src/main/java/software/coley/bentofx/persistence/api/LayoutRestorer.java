package software.coley.bentofx.persistence.api;

import software.coley.bentofx.persistence.impl.DockingLayout;

import java.util.function.Supplier;

/**
 * The Application Programming Interface for restoring a persisted BentoFX layout.
 *
 * @author Phil Bryant
 */
public interface LayoutRestorer {

    /**
     * Returns {@code true} if a stored layout exists; otherwise, returns {@code false}.
     *
     * @return {@code true} if a stored layout exists; otherwise, returns {@code false}.
     */
    boolean doesLayoutExist();

    /**
     * Returns the restored {@link DockingLayout}, if it exists and can be read
     * without error. Otherwise, returns the {@link DockingLayout} returned by
     * the {@code Supplier<DockingLayout>}.
     *
     * @param defaultLayoutSupplier the {@code Supplier<DockingLayout>} to use
     *                              when the {@link DockingLayout} does not
     *                              exist or cannot be restored without error.
     * @return the restored {@link DockingLayout}, if it exists and can be read
     * without error; otherwise, the {@link DockingLayout} returned by the
     * {@code Supplier<DockingLayout>}.
     */
    DockingLayout restoreLayout(
            final Supplier<DockingLayout> defaultLayoutSupplier
    );
}
