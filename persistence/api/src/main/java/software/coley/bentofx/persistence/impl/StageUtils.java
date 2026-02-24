/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl;

import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.control.IdentifiableStage;
import software.coley.bentofx.persistence.api.codec.IdentifiableStageState.StageStateBuilder;
import software.coley.bentofx.persistence.api.codec.StageState;

import java.util.List;

/**
 * Utility class containing methods relating to JavaFX {@link Stage}.
 *
 * @author Phil Bryant
 */
public class StageUtils {

    private StageUtils() {
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

    public static @NotNull StageStateBuilder getStageStateBuilder(
            final @NotNull IdentifiableStage stage
    ) {
        return new StageStateBuilder(stage.getIdentifier())
                .setTitle(stage.getTitle())
                .setX(stage.getX())
                .setY(stage.getY())
                .setWidth(stage.getWidth())
                .setHeight(stage.getHeight())
                .setModality(stage.getModality())
                .setOpacity(stage.getOpacity())
                .setIconified(stage.isIconified())
                .setFullScreen(stage.isFullScreen())
                .setMaximized(stage.isMaximized())
                .setAlwaysOnTop(stage.isAlwaysOnTop())
                .setResizable(stage.isResizable())
                .setShowing(stage.isShowing())
                .setFocused(stage.isFocused());
    }

    public static void applyStageState(
            final @NotNull StageState stageState,
            final @NotNull Stage stage
    ) {
        stageState.getTitle().ifPresent(stage::setTitle);
        stageState.getX().ifPresent(stage::setX);
        stageState.getY().ifPresent(stage::setY);
        stageState.getWidth().ifPresent(stage::setWidth);
        stageState.getHeight().ifPresent(stage::setHeight);
        stageState.getModality().ifPresent(stage::initModality);
        stageState.getOpacity().ifPresent(stage::setOpacity);
        stageState.isIconified().ifPresent(stage::setIconified);
        stageState.isFullScreen().ifPresent(stage::setFullScreen);
        stageState.isMaximized().ifPresent(stage::setMaximized);
        stageState.isAlwaysOnTop().ifPresent(stage::setAlwaysOnTop);
        stageState.isResizable().ifPresent(stage::setResizable);

        if (stageState.isFocused().orElse(false)) {
            stage.requestFocus();
        }

        if (stageState.isShowing().orElse(false)) {
            stage.show();
        }
    }
}
