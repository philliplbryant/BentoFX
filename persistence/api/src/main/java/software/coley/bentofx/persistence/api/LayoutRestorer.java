/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.layout.container.DockContainerRootBranch;

import java.util.function.Supplier;

/**
 * The Application Programming Interface for restoring a persisted BentoFX layout.
 *
 * @author Phil Bryant
 */
public interface LayoutRestorer {

    DockContainerRootBranch restoreLayout(
            final @NotNull Stage primaryStage,
            final @NotNull Supplier<DockContainerRootBranch> defaultLayoutSupplier
    );
}
