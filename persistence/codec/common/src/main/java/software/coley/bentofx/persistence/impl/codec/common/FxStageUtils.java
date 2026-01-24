/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common;

import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

/**
 * Utility class containing methods relating to JavaFX {@link Stage}.
 *
 * @author Phil Bryant
 */
public class FxStageUtils {

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
