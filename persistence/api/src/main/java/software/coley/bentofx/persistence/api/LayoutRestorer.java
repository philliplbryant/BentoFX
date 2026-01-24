/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import javafx.stage.Stage;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.codec.BentoStateException;

/**
 * The Application Programming Interface for restoring a persisted BentoFX layout.
 *
 * @author Phil Bryant
 */
public interface LayoutRestorer {

    DockContainerRootBranch restoreLayout(final Stage primaryStage)
            throws BentoStateException;
}
