package software.coley.bentofx.persistence.api.state;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.state.DragDropStageState.DragDropStageStateBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BentoStateTest {

    @Test
    void bentoStateCanRepresentNestedRootBranchesAndDetachedStages() {
        DockableState editor =
                new DockableStateBuilder("dockable:editor")
                        .setTitle("Editor")
                        .build();

        DockableState terminal =
                new DockableStateBuilder("dockable:terminal")
                        .setTitle("Terminal")
                        .build();

        DockContainerLeafState editorLeaf =
                new DockContainerLeafStateBuilder("leaf:editor")
                        .setSide(Side.TOP)
                        .setSelectedDockableStateIdentifier("dockable:editor")
                        .addChildDockableState(editor)
                        .build();

        DockContainerLeafState terminalLeaf =
                new DockContainerLeafStateBuilder("leaf:terminal")
                        .setSide(Side.BOTTOM)
                        .addChildDockableState(terminal)
                        .build();

        DockContainerRootBranchState root =
                new DockContainerRootBranchStateBuilder("root:main")
                        .setOrientation(Orientation.VERTICAL)
                        .addDividerPosition(0, 0.70)
                        .addDockContainerState(editorLeaf)
                        .addDockContainerState(terminalLeaf)
                        .build();

        DragDropStageState detached =
                new DragDropStageStateBuilder(false)
                        .setTitle("Floating tools")
                        .setDockContainerRootBranchState(root)
                        .build();

        BentoState state =
                new BentoStateBuilder("bento:workbench")
                        .addRootBranchState(root)
                        .addDragDropStageState(detached)
                        .build();

        assertThat(state.getIdentifier())
                .isEqualTo("bento:workbench");

        assertThat(state.getRootBranchStates())
                .singleElement()
                .satisfies(savedRoot -> {

                    assertThat(savedRoot.getIdentifier())
                            .isEqualTo("root:main");

                    assertThat(savedRoot.getOrientation())
                            .contains(Orientation.VERTICAL);

                    assertThat(savedRoot.getDividerPositions())
                            .containsEntry(0, 0.70);

                    assertThat(savedRoot.getChildDockContainerStates())
                            .hasSize(2);
                });

        assertThat(state.getDragDropStageStates())
                .singleElement()
                .satisfies(savedStage -> {

                    assertThat(savedStage.getTitle())
                            .contains("Floating tools");

                    assertThat(savedStage.isAutoClosedWhenEmpty())
                            .isFalse();

                    assertThat(savedStage.getDockContainerRootBranchState())
                            .contains(root);
                });

        final List<DockContainerRootBranchState> rootBranchStates =
                state.getRootBranchStates();
        assertThatThrownBy(rootBranchStates::clear)
                .isInstanceOf(UnsupportedOperationException.class);

        final List<DragDropStageState> dragDropStageStates =
                state.getDragDropStageStates();

        assertThatThrownBy(dragDropStageStates::clear)
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
