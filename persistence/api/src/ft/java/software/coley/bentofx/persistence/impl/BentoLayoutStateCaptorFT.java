package software.coley.bentofx.persistence.impl;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.state.*;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Side.RIGHT;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class BentoLayoutStateCaptorFT {

    private static final String BENTO_ID = "bento-state-captor";
    private static final String ROOT_BRANCH_ID = "root-state-captor";
    private static final String BRANCH_ID = "branch-state-captor";
    private static final String LEAF_ID = "leaf-state-captor";
    private static final String LEAF_DOCKABLE_ID = "leaf-dockable-state-captor";
    private static final String CAPTOR_STAGE_TITLE = "Bento Layout State Captor";

    private @Nullable Stage stage;

    @SuppressWarnings("unused") // invoked reflectively
    @Start
    void start(final Stage stage) {
        this.stage = stage;
    }

    @Test
    void captureBentoStatesCapturesNestedContainersAndDockables(
            final FxRobot robot
    ) {
        final DefaultBentoProvider bentoProvider = new DefaultBentoProvider();
        final AtomicReference<List<BentoState>> bentoStatesReference =
                new AtomicReference<>();

        assertThat(stage)
                .describedAs("stage")
                .isNotNull();

        robot.interact(() -> {
            final Bento bento = new Bento(BENTO_ID);
            final DockBuilding dockBuilding = bento.dockBuilding();

            final DockContainerRootBranch rootBranch =
                    dockBuilding.root(ROOT_BRANCH_ID);
            rootBranch.setOrientation(HORIZONTAL);
            rootBranch.setPruneWhenEmpty(false);

            final DockContainerBranch branch = dockBuilding.branch(BRANCH_ID);
            branch.orientationProperty().set(VERTICAL);
            branch.setPruneWhenEmpty(true);

            final DockContainerLeaf leaf = dockBuilding.leaf(LEAF_ID);
            leaf.setSide(RIGHT);
            leaf.setCanSplit(false);
            leaf.setPruneWhenEmpty(false);

            final Dockable leafDockable = dockBuilding.dockable(LEAF_DOCKABLE_ID);
            leaf.addDockable(leafDockable);
            leaf.selectDockable(leafDockable);

            branch.addContainer(leaf);
            rootBranch.addContainer(branch);

            stage.setTitle(CAPTOR_STAGE_TITLE);
            stage.setScene(new Scene(rootBranch));
            stage.show();

            bentoProvider.addBento(bento);

            bentoStatesReference.set(
                    new BentoLayoutStateCaptor(bentoProvider)
                            .captureBentoStates()
            );
        });

        try {
            final List<BentoState> bentoStates = bentoStatesReference.get();
            assertThat(bentoStates)
                    .describedAs("bentoStates")
                    .hasSize(1);

            final BentoState bentoState = bentoStates.getFirst();
            assertThat(bentoState.getIdentifier())
                    .describedAs("bentoState.getIdentifier()")
                    .isEqualTo(BENTO_ID);
            assertThat(bentoState.getDragDropStageStates())
                    .describedAs("bentoState.getDragDropStageStates()")
                    .isEmpty();
            assertThat(bentoState.getRootBranchStates())
                    .describedAs("bentoState.getRootBranchStates()")
                    .hasSize(1);

            final DockContainerRootBranchState rootBranchState =
                    bentoState.getRootBranchStates().getFirst();
            assertThat(rootBranchState.getIdentifier())
                    .describedAs("rootBranchState.getIdentifier()")
                    .isEqualTo(ROOT_BRANCH_ID);
            assertThat(rootBranchState.getOrientation())
                    .describedAs("rootBranchState.getOrientation()")
                    .contains(HORIZONTAL);
            assertThat(rootBranchState.doPruneWhenEmpty())
                    .describedAs("rootBranchState.doPruneWhenEmpty()")
                    .contains(false);
            assertThat(rootBranchState.getChildDockableStates())
                    .describedAs("rootBranchState.getChildDockableStates()")
                    .isEmpty();
            assertThat(rootBranchState.getChildDockContainerStates())
                    .describedAs("rootBranchState.getChildDockContainerStates()")
                    .hasSize(1);

            final DockContainerBranchState branchState =
                    (DockContainerBranchState) rootBranchState
                            .getChildDockContainerStates()
                            .getFirst();
            assertThat(branchState.getIdentifier())
                    .describedAs("branchState.getIdentifier()")
                    .isEqualTo(BRANCH_ID);
            assertThat(branchState.getOrientation())
                    .describedAs("branchState.getOrientation()")
                    .contains(VERTICAL);
            assertThat(branchState.doPruneWhenEmpty())
                    .describedAs("branchState.doPruneWhenEmpty()")
                    .contains(true);
            assertThat(branchState.getChildDockContainerStates())
                    .describedAs("branchState.getChildDockContainerStates()")
                    .hasSize(1);

            final DockContainerLeafState leafState =
                    (DockContainerLeafState) branchState
                            .getChildDockContainerStates()
                            .getFirst();
            assertThat(leafState.getIdentifier())
                    .describedAs("leafState.getIdentifier()")
                    .isEqualTo(LEAF_ID);
            assertThat(leafState.getSide())
                    .describedAs("leafState.getSide()")
                    .contains(RIGHT);
            assertThat(leafState.isCanSplit())
                    .describedAs("leafState.isCanSplit()")
                    .contains(false);
            assertThat(leafState.doPruneWhenEmpty())
                    .describedAs("leafState.doPruneWhenEmpty()")
                    .contains(false);
            assertThat(leafState.getSelectedDockableIdentifier())
                    .describedAs("leafState.getSelectedDockableIdentifier()")
                    .contains(LEAF_DOCKABLE_ID);
            assertThat(leafState.getChildDockableStates())
                    .describedAs("leafState.getChildDockableStates()")
                    .extracting(DockableState::getIdentifier)
                    .containsExactly(LEAF_DOCKABLE_ID);
        } finally {
            robot.interact(stage::hide);
        }
    }
}
