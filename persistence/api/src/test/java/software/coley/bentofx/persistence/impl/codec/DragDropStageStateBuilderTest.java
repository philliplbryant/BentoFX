package software.coley.bentofx.persistence.impl.codec;

import javafx.geometry.Orientation;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.impl.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DragDropStageState.DragDropStageStateBuilder;

import static javafx.stage.Modality.NONE;
import static org.assertj.core.api.Assertions.assertThat;

class DragDropStageStateBuilderTest {

    @Test
    void dragDropStageBuilderCapturesOptionalStageMetadata() {
        DockContainerRootBranchState rootState =
                new DockContainerRootBranchStateBuilder("root-1")
                        .setOrientation(Orientation.HORIZONTAL)
                        .build();

        DragDropStageState stageState =
                new DragDropStageStateBuilder(true)
                        .setDockContainerRootBranchState(rootState)
                        .setTitle("Detached")
                        .setX(10.0)
                        .setY(20.0)
                        .setWidth(800.0)
                        .setHeight(600.0)
                        .setModality(NONE)
                        .setOpacity(0.85)
                        .setIconified(false)
                        .setFullScreen(false)
                        .setMaximized(true)
                        .setAlwaysOnTop(true)
                        .setResizable(true)
                        .setShowing(true)
                        .setFocused(false)
                        .build();

        assertThat(stageState.isAutoClosedWhenEmpty())
                .isTrue();

        assertThat(stageState.getDockContainerRootBranchState())
                .contains(rootState);

        assertThat(stageState.getTitle())
                .contains("Detached");

        assertThat(stageState.getX())
                .contains(10.0);

        assertThat(stageState.getY())
                .contains(20.0);

        assertThat(stageState.getWidth())
                .contains(800.0);

        assertThat(stageState.getHeight())
                .contains(600.0);

        assertThat(stageState.getModality())
                .contains(NONE);

        assertThat(stageState.getOpacity())
                .contains(0.85);

        assertThat(stageState.isIconified())
                .contains(false);

        assertThat(stageState.isFullScreen())
                .contains(false);

        assertThat(stageState.isMaximized())
                .contains(true);

        assertThat(stageState.isAlwaysOnTop())
                .contains(true);

        assertThat(stageState.isResizable())
                .contains(true);

        assertThat(stageState.isShowing())
                .contains(true);

        assertThat(stageState.isFocused())
                .contains(false);
    }
}
