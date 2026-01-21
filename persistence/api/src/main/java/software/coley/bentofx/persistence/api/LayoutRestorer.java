/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import javafx.scene.Parent;
import javafx.stage.Stage;
import software.coley.bentofx.persistence.api.codec.BentoStateException;

/**
 * The Application Programming Interface for restoring a persisted BentoFX layout.
 */
public interface LayoutRestorer {

    Parent restoreLayout(final Stage primaryStage) throws BentoStateException;
}
