package software.coley.bentofx.persistence.impl;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.WritableImage;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.impl.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.impl.codec.BentoState;
import software.coley.bentofx.persistence.impl.codec.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockableState;
import software.coley.bentofx.persistence.impl.codec.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.bentofx.persistence.testfixtures.codec.InMemoryLayoutCodec;
import software.coley.bentofx.persistence.testfixtures.storage.InMemoryLayoutStorage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Side.LEFT;
import static javafx.stage.Modality.NONE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class BentoLayoutRestorerFT {

    @Test
    void restoreLayoutReturnsDefaultWhenStorageDoesNotExist() throws Exception {
        InMemoryLayoutStorage storage = new InMemoryLayoutStorage();
        InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        DockingLayout defaultLayout = new DockingLayoutBuilder().build();
        BentoProvider bentoProvider = new DefaultBentoProvider(new Bento());

        DockingLayoutRestorer layoutRestorer = new DockingLayoutRestorer(
                codec,
                storage,
                bentoProvider,
                id -> Optional.empty(),
                null,
                null
        );

        DockingLayout restoredLayout = layoutRestorer.restoreLayout(() -> defaultLayout);

        assertThat(restoredLayout).isSameAs(defaultLayout);
        assertThat(storage.exists()).isFalse();
        assertThat(codec.getEncodeCalls()).isEmpty();
    }

    @Test
    void restoreLayoutBuildsRootBranchesAndDragDropStages(FxRobot robot) throws Exception {

        // Dockable state
        final String expectedDockableId = "Dockable ID";
        final String expectedDockableTitle = "Dockable Title";
        final String expectedDockableTooltipText = "This is the Dockable's tooltip text";
        final boolean expectedDockableIsClosable = true;

        // Leaf state
        final String expectedLeafId = "Leaf ID";
        final String expectedSecondLeafId = "Second Leaf ID";
        final Side expectedLeafSide = LEFT;
        final boolean expectedLeafCanSplit = true;
        final boolean expectedLeafResizableWithParent = true;
        final double expectedLeafUncollapsedSizePx = 240.0;
        final boolean expectedLeafIsCollapsed = false;
        final boolean expectedLeafPruneWhenEmpty = false;

        // Bento state
        final String expectedBentoId = "Bento ID";
        final String expectedBentoRootId = "Bento Root ID";

        // DragDropStage root branch state
        final String expectedDragRootBuilderId = "DragDropStage Root Branch ID";
        final Orientation expectedDragRootOrientation = VERTICAL;
        final boolean expectedDragRootPruneWhenEmpty = true;

        // DragDropStage state
        final String expectedDragDropStageTitle = "DragDropStage ID";
        final double expectedX = 120.0;
        final double expectedY = 75.0;
        final double expectedWidth = 640.0;
        final double expectedHeight = 480.0;
        final double expectedOpacity = 0.8;
        final boolean expectedResizable = true;
        final boolean expectedAlwaysOnTop = false;
        final Modality expectedModality = NONE;

        // Primary root branch state
        final Orientation expectedRootOrientation = HORIZONTAL;
        final boolean expectedRootPruneWhenEmpty = true;
        final double expectedRootDividerPosition = 0.35;

        DockableState dockableState = new DockableStateBuilder(expectedDockableId)
                .setTitle(expectedDockableTitle)
                .setTooltip(expectedDockableTooltipText)
                .setDockableNode(new Label(expectedDockableId))
                .setClosable(expectedDockableIsClosable)
                .build();

        DockContainerLeafStateBuilder leafBuilder =
                new DockContainerLeafStateBuilder(expectedLeafId);
        leafBuilder.setSide(expectedLeafSide);
        leafBuilder.setCanSplit(expectedLeafCanSplit);
        leafBuilder.setResizableWithParent(expectedLeafResizableWithParent);
        leafBuilder.setSelectedDockableStateIdentifier(expectedDockableId);
        leafBuilder.setUncollapsedSizePx(expectedLeafUncollapsedSizePx);
        leafBuilder.setCollapsed(expectedLeafIsCollapsed);
        leafBuilder.setPruneWhenEmpty(expectedLeafPruneWhenEmpty);
        leafBuilder.addChildDockableState(dockableState);

        DockContainerRootBranchStateBuilder rootBuilder =
                new DockContainerRootBranchStateBuilder(expectedBentoRootId);
        rootBuilder.setOrientation(expectedRootOrientation);
        rootBuilder.setPruneWhenEmpty(expectedRootPruneWhenEmpty);
        rootBuilder.addDividerPosition(0, expectedRootDividerPosition);
        rootBuilder.addDockContainerState(leafBuilder.build());
        rootBuilder.addDockContainerState(
                new DockContainerLeafStateBuilder(expectedSecondLeafId).build()
        );

        DockContainerRootBranchStateBuilder dragRootBuilder =
                new DockContainerRootBranchStateBuilder(expectedDragRootBuilderId);
        dragRootBuilder.setOrientation(expectedDragRootOrientation);
        dragRootBuilder.setPruneWhenEmpty(expectedDragRootPruneWhenEmpty);

        BentoState state = new BentoState.BentoStateBuilder(expectedBentoId)
                .addRootBranchState(rootBuilder.build())
                .addDragDropStageState(new DragDropStageStateBuilder(true)
                        .setTitle(expectedDragDropStageTitle)
                        .setX(expectedX)
                        .setY(expectedY)
                        .setWidth(expectedWidth)
                        .setHeight(expectedHeight)
                        .setOpacity(expectedOpacity)
                        .setResizable(expectedResizable)
                        .setAlwaysOnTop(expectedAlwaysOnTop)
                        .setModality(expectedModality)
                        .setDockContainerRootBranchState(dragRootBuilder.build())
                        .build())
                .build();

        InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        InMemoryLayoutStorage storage = new InMemoryLayoutStorage();
        try (var out = storage.openOutputStream()) {
            codec.writeEncoded(List.of(state), out);
        }

        StageIconImageProvider stageIconImageProvider =
                () -> List.of(
                        new WritableImage(1, 1)
                );

        DockingLayoutRestorer restorer = new DockingLayoutRestorer(
                codec,
                storage,
                new DefaultBentoProvider(new Bento(expectedBentoId)),
                actualId ->
                        actualId.equals(expectedDockableId)
                                ? Optional.of(dockableState)
                                : Optional.empty(),
                stageIconImageProvider,
                null
        );

        AtomicReference<DockingLayout> restoredRef = new AtomicReference<>();
        robot.interact(() ->
                restoredRef.set(
                        restorer.restoreLayout(
                                () ->
                                        new DockingLayoutBuilder().build()
                        )
                )
        );

        DockingLayout restored = restoredRef.get();
        assertThat(restored.getBentoLayouts())
                .hasSize(1);

        BentoLayout bentoLayout = restored.getBentoLayouts().getFirst();
        assertThat(bentoLayout.getIdentifier())
                .isEqualTo(expectedBentoId);

        assertThat(bentoLayout.getRootBranches())
                .hasSize(1);
        assertThat(bentoLayout.getDragDropStages())
                .hasSize(1);

        DockContainerRootBranch root = bentoLayout.getRootBranches().getFirst();
        assertThat(root.getIdentifier())
                .isEqualTo(expectedBentoRootId);
        assertThat(root.getOrientation())
                .isEqualTo(expectedRootOrientation);
        assertThat(root.doPruneWhenEmpty())
                .isEqualTo(expectedRootPruneWhenEmpty);
        assertThat(root.getDividerPositions())
                .containsExactly(expectedRootDividerPosition);

        List<DockContainer> rootContainers = root.getChildContainers();
        assertThat(rootContainers)
                .hasSize(2);

        DockContainer dockContainer = rootContainers.getFirst();
        assertThat(dockContainer).isInstanceOf(DockContainerLeaf.class);
        assertThat(dockContainer.getIdentifier())
                .isEqualTo(expectedLeafId);
        final DockContainerLeaf leaf = (DockContainerLeaf) dockContainer;
        assertThat(leaf.getSide())
                .isEqualTo(expectedLeafSide);
        assertThat(leaf.isCanSplit())
                .isEqualTo(expectedLeafCanSplit);
        assertThat(SplitPane.isResizableWithParent(leaf))
                .isEqualTo(expectedLeafResizableWithParent);
        assertThat(leaf.isCollapsed())
                .isEqualTo(expectedLeafIsCollapsed);
        assertThat(leaf.doPruneWhenEmpty())
                .isEqualTo(expectedLeafPruneWhenEmpty);

        List<Dockable> dockables = dockContainer.getDockables();
        assertThat(dockables)
                .hasSize(1);
        assertThat(dockables.getFirst().getIdentifier())
                .isEqualTo(expectedDockableId);
        assertThat(dockables.getFirst().getTitle())
                .isEqualTo(expectedDockableTitle);
        assertThat(dockables.getFirst().getTooltip().getText())
                .isEqualTo(expectedDockableTooltipText);
        assertThat(dockables.getFirst().isClosable())
                .isEqualTo(expectedDockableIsClosable);

        Stage dragStage = bentoLayout.getDragDropStages().getFirst();
        assertThat(dragStage.getTitle())
                .isEqualTo(expectedDragDropStageTitle);
        assertThat(dragStage.getX())
                .isEqualTo(expectedX);
        assertThat(dragStage.getY())
                .isEqualTo(expectedY);
        assertThat(dragStage.getWidth())
                .isEqualTo(expectedWidth);
        assertThat(dragStage.getHeight())
                .isEqualTo(expectedHeight);
        assertThat(dragStage.getOpacity())
                .isEqualTo(expectedOpacity);
        assertThat(dragStage.isResizable())
                .isEqualTo(expectedResizable);
        assertThat(dragStage.isAlwaysOnTop())
                .isEqualTo(expectedAlwaysOnTop);
        assertThat(dragStage.getModality())
                .isEqualTo(expectedModality);
        assertThat(dragStage.getIcons())
                .hasSize(1);
        assertThat(dragStage.getScene().getRoot())
                .isInstanceOf(DockContainerRootBranch.class);
        final DockContainerRootBranch rootBranch = (DockContainerRootBranch) dragStage.getScene().getRoot();
        assertThat(rootBranch.getIdentifier())
                .isEqualTo(expectedDragRootBuilderId);
        assertThat(rootBranch.getOrientation())
                .isEqualTo(expectedDragRootOrientation);
        assertThat(rootBranch.doPruneWhenEmpty())
                .isEqualTo(expectedDragRootPruneWhenEmpty);

        robot.interact(dragStage::hide);
    }
}
