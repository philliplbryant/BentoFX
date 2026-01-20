/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common;

import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class FxStageUtils {

    public static final String IS_PRIMARY_STAGE_PROPERTY_KEY_NAME = "isPrimaryStage";
    public static final String PRIMARY_STAGE_ID = "primaryStage";
    public static final String STAGE_ID_PROPERTY_KEY_NAME = "stageId";

    private FxStageUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns an immutable {@link List} containing all {@link Stage} instances.
     *
     * @return an immutable {@link List} containing all {@link Stage} instances.
     */
    public static List<Stage> getAllStages() {
        return Window.getWindows()
                     .stream()
                     .filter(
                             Stage.class::isInstance
                     )
                     .map(
                             Stage.class::cast
                     )
                     .toList();
    }
}
