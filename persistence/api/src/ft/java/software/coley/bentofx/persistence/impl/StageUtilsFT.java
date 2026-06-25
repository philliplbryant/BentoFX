package software.coley.bentofx.persistence.impl;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class StageUtilsFT {

    private static final String FIRST_STAGE_TITLE = "first";
    private static final String SECOND_STAGE_TITLE = "second";

    private @Nullable Stage first;
    private @Nullable Stage second;
    private @Nullable Popup popup;

    @Start
    private void start(Stage ignored) {
        first = new Stage();
        first.setTitle(FIRST_STAGE_TITLE);
        first.setScene(new Scene(new Label("one"), 100, 50));
        first.show();

        second = new Stage();
        second.setTitle(SECOND_STAGE_TITLE);
        second.setScene(new Scene(new Label("two"), 100, 50));
        second.show();

        popup = new Popup();
        popup.getContent().add(new Label("popup"));
        popup.show(first);
    }


    @Test
    void getAllStagesReturnsOnlyJavaFxStages(final FxRobot robot) {
        final Stage firstStage = getFirstStage();
        final Stage secondStage = getSecondStage();
        final Popup activePopup = getPopup();
        final List<Stage> stages = StageUtils.getAllStages();

        assertThat(stages)
                .describedAs("stages")
                .contains(firstStage, secondStage);
        assertThat(stages.stream().map(Stage::getTitle))
                .describedAs("stages.stream().map(Stage::getTitle)")
                .containsOnly(FIRST_STAGE_TITLE, SECOND_STAGE_TITLE);
        assertThat(Window.getWindows())
                .describedAs("Window.getWindows()")
                .contains(activePopup);

        robot.interact(() -> {
            activePopup.hide();
            secondStage.hide();
            firstStage.hide();
        });
    }

    private Stage getFirstStage() {
        final Stage activeFirst = first;
        assertThat(activeFirst)
                .describedAs("first")
                .isNotNull();
        return activeFirst;
    }

    private Stage getSecondStage() {
        final Stage activeSecond = second;
        assertThat(activeSecond)
                .describedAs("second")
                .isNotNull();
        return activeSecond;
    }

    private Popup getPopup() {
        final Popup activePopup = popup;
        assertThat(activePopup)
                .describedAs("popup")
                .isNotNull();
        return activePopup;
    }
}
