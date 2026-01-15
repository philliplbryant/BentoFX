/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import javafx.scene.Parent;
import javafx.stage.Stage;
import software.coley.bentofx.persistence.api.codec.BentoStateException;

public interface LayoutRestorer {

    Parent restoreLayout(final Stage primaryStage) throws BentoStateException;
}
