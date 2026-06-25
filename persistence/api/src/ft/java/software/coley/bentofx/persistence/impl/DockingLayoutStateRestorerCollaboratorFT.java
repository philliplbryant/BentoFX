package software.coley.bentofx.persistence.impl;

import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoLayout;
import software.coley.bentofx.persistence.api.DockingLayout;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockableState;
import software.coley.bentofx.persistence.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Side.LEFT;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class DockingLayoutStateRestorerCollaboratorFT {

    private static final String BENTO_ID = "bento-state-restorer";
    private static final String ROOT_BRANCH_ID = "root-state-restorer";
    private static final String BRANCH_ID = "branch-state-restorer";
    private static final String LEAF_ID = "leaf-state-restorer";
    private static final String DOCKABLE_ID = "dockable-state-restorer";
    private static final String MISSING_DOCKABLE_ID = "missing-dockable-state-restorer";
    private static final String DOCKABLE_TITLE = "Restored Dockable";
    private static final String DOCKABLE_TOOLTIP_TEXT = "Restored Dockable Tooltip";

    @Test
    void restoreDockingLayoutRestoresNestedContainersAndDockableProperties(
            final FxRobot robot
    ) {
        final AtomicBoolean dockableConsumerCalled = new AtomicBoolean();
        final AtomicReference<DockingLayout> dockingLayoutReference =
                new AtomicReference<>();

        robot.interact(() -> {
            final DockableState dockableState =
                    new DockableStateBuilder(DOCKABLE_ID)
                            .setTitle(DOCKABLE_TITLE)
                            .setTooltipText(DOCKABLE_TOOLTIP_TEXT)
                            .setDockableNode(new Label(DOCKABLE_ID))
                            .setClosable(false)
                            .setDockableConsumer(dockable ->
                                    dockableConsumerCalled.set(true)
                            )
                            .build();

            final DockContainerLeafStateBuilder leafStateBuilder =
                    new DockContainerLeafStateBuilder(LEAF_ID);
            leafStateBuilder.setSide(LEFT);
            leafStateBuilder.setCanSplit(false);
            leafStateBuilder.setResizableWithParent(false);
            leafStateBuilder.setSelectedDockableStateIdentifier(DOCKABLE_ID);
            leafStateBuilder.setPruneWhenEmpty(false);
            leafStateBuilder.addChildDockableState(dockableState);

            final DockContainerBranchStateBuilder branchStateBuilder =
                    new DockContainerBranchStateBuilder(BRANCH_ID);
            branchStateBuilder.setOrientation(VERTICAL);
            branchStateBuilder.setPruneWhenEmpty(true);
            branchStateBuilder.addDockContainerState(leafStateBuilder.build());

            final DockContainerRootBranchStateBuilder rootBranchStateBuilder =
                    new DockContainerRootBranchStateBuilder(ROOT_BRANCH_ID);
            rootBranchStateBuilder.setOrientation(HORIZONTAL);
            rootBranchStateBuilder.setPruneWhenEmpty(false);
            rootBranchStateBuilder.addDockContainerState(branchStateBuilder.build());

            final BentoState bentoState =
                    new BentoState.BentoStateBuilder(BENTO_ID)
                            .addRootBranchState(rootBranchStateBuilder.build())
                            .build();

            dockingLayoutReference.set(
                    new DockingLayoutStateRestorer(
                            new DefaultBentoProvider(new Bento(BENTO_ID)),
                            actualId -> actualId.equals(DOCKABLE_ID)
                                    ? Optional.of(dockableState)
                                    : Optional.empty(),
                            null,
                            null
                    ).restoreDockingLayout(List.of(bentoState))
            );
        });

        final DockingLayout dockingLayout = dockingLayoutReference.get();
        assertThat(dockingLayout.getBentoLayouts())
                .describedAs("dockingLayout.getBentoLayouts()")
                .hasSize(1);

        final BentoLayout bentoLayout = dockingLayout.getBentoLayouts().getFirst();
        assertThat(bentoLayout.getIdentifier())
                .describedAs("bentoLayout.getIdentifier()")
                .isEqualTo(BENTO_ID);
        assertThat(bentoLayout.getRootBranches())
                .describedAs("bentoLayout.getRootBranches()")
                .hasSize(1);
        assertThat(bentoLayout.getDragDropStages())
                .describedAs("bentoLayout.getDragDropStages()")
                .isEmpty();

        final DockContainerRootBranch rootBranch =
                bentoLayout.getRootBranches().getFirst();
        assertThat(rootBranch.getIdentifier())
                .describedAs("rootBranch.getIdentifier()")
                .isEqualTo(ROOT_BRANCH_ID);
        assertThat(rootBranch.getOrientation())
                .describedAs("rootBranch.getOrientation()")
                .isEqualTo(HORIZONTAL);
        assertThat(rootBranch.doPruneWhenEmpty())
                .describedAs("rootBranch.doPruneWhenEmpty()")
                .isFalse();
        assertThat(rootBranch.getChildContainers())
                .describedAs("rootBranch.getChildContainers()")
                .hasSize(1);

        final DockContainer rootChild = rootBranch.getChildContainers().getFirst();
        assertThat(rootChild)
                .describedAs("rootChild")
                .isInstanceOf(DockContainerBranch.class);

        final DockContainerBranch branch = (DockContainerBranch) rootChild;
        assertThat(branch.getIdentifier())
                .describedAs("branch.getIdentifier()")
                .isEqualTo(BRANCH_ID);
        assertThat(branch.orientationProperty().get())
                .describedAs("branch.orientationProperty().get()")
                .isEqualTo(VERTICAL);
        assertThat(branch.doPruneWhenEmpty())
                .describedAs("branch.doPruneWhenEmpty()")
                .isTrue();
        assertThat(branch.getChildContainers())
                .describedAs("branch.getChildContainers()")
                .hasSize(1);

        final DockContainer branchChild = branch.getChildContainers().getFirst();
        assertThat(branchChild)
                .describedAs("branchChild")
                .isInstanceOf(DockContainerLeaf.class);

        final DockContainerLeaf leaf = (DockContainerLeaf) branchChild;
        assertThat(leaf.getIdentifier())
                .describedAs("leaf.getIdentifier()")
                .isEqualTo(LEAF_ID);
        assertThat(leaf.getSide())
                .describedAs("leaf.getSide()")
                .isEqualTo(LEFT);
        assertThat(leaf.isCanSplit())
                .describedAs("leaf.isCanSplit()")
                .isFalse();
        assertThat(SplitPane.isResizableWithParent(leaf))
                .describedAs("SplitPane.isResizableWithParent(leaf)")
                .isFalse();
        assertThat(leaf.doPruneWhenEmpty())
                .describedAs("leaf.doPruneWhenEmpty()")
                .isFalse();
        assertThat(leaf.getDockables())
                .describedAs("leaf.getDockables()")
                .hasSize(1);

        final Dockable dockable = leaf.getDockables().getFirst();
        assertThat(dockable.getIdentifier())
                .describedAs("dockable.getIdentifier()")
                .isEqualTo(DOCKABLE_ID);
        assertThat(dockable.getTitle())
                .describedAs("dockable.getTitle()")
                .isEqualTo(DOCKABLE_TITLE);
        assertThat(dockable.getTooltip())
                .describedAs("dockable.getTooltip()")
                .isInstanceOf(Tooltip.class);
        assertThat(dockable.getTooltip().getText())
                .describedAs("dockable.getTooltip().getText()")
                .isEqualTo(DOCKABLE_TOOLTIP_TEXT);
        assertThat(dockable.isClosable())
                .describedAs("dockable.isClosable()")
                .isFalse();
        assertThat(leaf.getSelectedDockable())
                .describedAs("leaf.getSelectedDockable()")
                .isSameAs(dockable);
        assertThat(dockableConsumerCalled.get())
                .describedAs("dockableConsumerCalled.get()")
                .isTrue();
    }

    @Test
    void restoreDockingLayoutSkipsUnresolvedDockableStates(
            final FxRobot robot
    ) {
        final AtomicReference<DockingLayout> dockingLayoutReference =
                new AtomicReference<>();

        robot.interact(() -> {
            final DockableState missingDockableState =
                    new DockableStateBuilder(MISSING_DOCKABLE_ID)
                            .setDockableNode(new Label(MISSING_DOCKABLE_ID))
                            .build();

            final DockContainerLeafStateBuilder leafStateBuilder =
                    new DockContainerLeafStateBuilder(LEAF_ID);
            leafStateBuilder.addChildDockableState(missingDockableState);

            final DockContainerRootBranchStateBuilder rootBranchStateBuilder =
                    new DockContainerRootBranchStateBuilder(ROOT_BRANCH_ID);
            rootBranchStateBuilder.addDockContainerState(leafStateBuilder.build());

            final BentoState bentoState =
                    new BentoState.BentoStateBuilder(BENTO_ID)
                            .addRootBranchState(rootBranchStateBuilder.build())
                            .build();

            dockingLayoutReference.set(
                    new DockingLayoutStateRestorer(
                            new DefaultBentoProvider(new Bento(BENTO_ID)),
                            actualId -> Optional.empty(),
                            null,
                            null
                    ).restoreDockingLayout(List.of(bentoState))
            );
        });

        final DockContainerRootBranch rootBranch = dockingLayoutReference.get()
                .getBentoLayouts()
                .getFirst()
                .getRootBranches()
                .getFirst();
        final DockContainerLeaf leaf =
                (DockContainerLeaf) rootBranch.getChildContainers().getFirst();

        assertThat(leaf.getIdentifier())
                .describedAs("leaf.getIdentifier()")
                .isEqualTo(LEAF_ID);
        assertThat(leaf.getDockables())
                .describedAs("leaf.getDockables()")
                .isEmpty();
    }
}
