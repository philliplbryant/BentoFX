package software.coley.bentofx.persistence.impl;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class StageUtilsFT {

    private Stage first;
    private Stage second;
    private Popup popup;

    @Start
    private void start(Stage ignored) {
        first = new Stage();
        first.setTitle("first");
        first.setScene(new Scene(new Label("one"), 100, 50));
        first.show();

        second = new Stage();
        second.setTitle("second");
        second.setScene(new Scene(new Label("two"), 100, 50));
        second.show();

        popup = new Popup();
        popup.getContent().add(new Label("popup"));
        popup.show(first);
    }


    @Test
    void getAllStagesReturnsOnlyJavaFxStages(final FxRobot robot) {
        List<Stage> stages = StageUtils.getAllStages();

        assertThat(stages)
                .contains(first, second);
        assertThat(stages.stream().map(Stage::getTitle))
                .containsOnly("first", "second");
        assertThat(Window.getWindows())
                .contains(popup);

        robot.interact(() -> {
            popup.hide();
            second.hide();
            first.hide();
        });
    }
}
