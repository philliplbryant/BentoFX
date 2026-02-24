/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.storage.DockingLayout;

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

    @NotNull DockingLayout restoreLayout(
            final @NotNull Supplier<DockingLayout> defaultLayoutSupplier
    );
}
