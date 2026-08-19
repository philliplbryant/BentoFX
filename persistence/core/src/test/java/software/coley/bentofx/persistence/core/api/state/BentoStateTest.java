package software.coley.bentofx.persistence.core.api.state;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.core.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DragDropStageState.DragDropStageStateBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BentoStateTest {

    private static final String BENTO_IDENTIFIER = "bento:workbench";
    private static final String ROOT_BRANCH_IDENTIFIER = "root:main";
    private static final String DOCKABLE_IDENTIFIER = "dockable:editor";
    private static final String STAGE_TITLE = "Floating tools";

    private static final String STATE_GET_ROOTBRANCHSTATES_DESCRIPTION = "state.getRootBranchStates()";

    @Test
    void bentoStateCanRepresentNestedRootBranchesAndDetachedStages() {
        DockableState editor =
                new DockableStateBuilder(DOCKABLE_IDENTIFIER)
                        .setTitle("Editor")
                        .build();

        DockableState terminal =
                new DockableStateBuilder("dockable:terminal")
                        .setTitle("Terminal")
                        .build();

        DockContainerLeafState editorLeaf =
                new DockContainerLeafStateBuilder("leaf:editor")
                        .setSide(Side.TOP)
                        .setSelectedDockableStateIdentifier(DOCKABLE_IDENTIFIER)
                        .addChildDockableState(editor)
                        .build();

        DockContainerLeafState terminalLeaf =
                new DockContainerLeafStateBuilder("leaf:terminal")
                        .setSide(Side.BOTTOM)
                        .addChildDockableState(terminal)
                        .build();

        DockContainerRootBranchState root =
                new DockContainerRootBranchStateBuilder(ROOT_BRANCH_IDENTIFIER)
                        .setOrientation(Orientation.VERTICAL)
                        .addDividerPosition(0, 0.70)
                        .addDockContainerState(editorLeaf)
                        .addDockContainerState(terminalLeaf)
                        .build();

        DragDropStageState detached =
                new DragDropStageStateBuilder(false)
                        .setTitle(STAGE_TITLE)
                        .setDockContainerRootBranchState(root)
                        .build();

        BentoState state =
                new BentoStateBuilder(BENTO_IDENTIFIER)
                        .addRootBranchState(root)
                        .addDragDropStageState(detached)
                        .build();

        assertThat(state.getIdentifier())
                .describedAs("state.getIdentifier()")
                .isEqualTo(BENTO_IDENTIFIER);

        assertThat(state.getRootBranchStates())
                .describedAs(STATE_GET_ROOTBRANCHSTATES_DESCRIPTION)
                .singleElement()
                .satisfies(savedRoot -> {

                    assertThat(savedRoot.getIdentifier())
                            .describedAs("savedRoot.getIdentifier()")
                            .isEqualTo(ROOT_BRANCH_IDENTIFIER);

                    assertThat(savedRoot.getOrientation())
                            .describedAs("savedRoot.getOrientation()")
                            .contains(Orientation.VERTICAL);

                    assertThat(savedRoot.getDividerPositions())
                            .describedAs("savedRoot.getDividerPositions()")
                            .containsEntry(0, 0.70);

                    assertThat(savedRoot.getChildDockContainerStates())
                            .describedAs("savedRoot.getChildDockContainerStates()")
                            .hasSize(2);
                });

        assertThat(state.getDragDropStageStates())
                .describedAs("state.getDragDropStageStates()")
                .singleElement()
                .satisfies(savedStage -> {

                    assertThat(savedStage.getTitle())
                            .contains(STAGE_TITLE);

                    assertThat(savedStage.isAutoClosedWhenEmpty())
                            .describedAs("savedStage.isAutoClosedWhenEmpty()")
                            .isFalse();

                    assertThat(savedStage.getDockContainerRootBranchState())
                            .describedAs("savedStage.getDockContainerRootBranchState()")
                            .contains(root);
                });

        final List<DockContainerRootBranchState> rootBranchStates =
                state.getRootBranchStates();
        assertThatThrownBy(rootBranchStates::clear)
                .describedAs("exception thrown by rootBranchStates::clear")
                .isInstanceOf(UnsupportedOperationException.class);

        final List<DragDropStageState> dragDropStageStates =
                state.getDragDropStageStates();

        assertThatThrownBy(dragDropStageStates::clear)
                .describedAs("exception thrown by dragDropStageStates::clear")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void builtStateIsNotAffectedByLaterBuilderMutation() {
        DockContainerRootBranchState firstRoot =
                new DockContainerRootBranchStateBuilder("root:first")
                        .build();
        DockContainerRootBranchState secondRoot =
                new DockContainerRootBranchStateBuilder("root:second")
                        .build();

        BentoStateBuilder builder =
                new BentoStateBuilder(BENTO_IDENTIFIER)
                        .addRootBranchState(firstRoot);

        BentoState state = builder.build();

        builder.addRootBranchState(secondRoot);

        assertThat(state.getRootBranchStates())
                .describedAs(STATE_GET_ROOTBRANCHSTATES_DESCRIPTION)
                .containsExactly(firstRoot);
    }

}
