package software.coley.bentofx.persistence.testfixtures.codec.dto;

import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.*;

import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static javafx.geometry.Side.TOP;
import static javafx.stage.Modality.NONE;

/**
 * Creates representative docking layout DTOs for codec and mapper tests.
 */
public final class SampleDockingLayoutDtoFactory {
    public static final String BENTO_IDENTIFIER = "bento-1";
    public static final String BRANCH_IDENTIFIER = "branch-1";
    public static final String DOCKABLE_IDENTIFIER = "dockable-1";
    public static final String LEAF_IDENTIFIER = "leaf-1";
    public static final String ROOT_IDENTIFIER = "root-1";
    public static final String STAGE_TITLE = "Stage";

    private static final int DIVIDER_INDEX = 0;
    private static final double DIVIDER_POSITION = 0.42;
    private static final double LEAF_UNCOLLAPSED_SIZE_PX = 321.0;
    private static final double STAGE_X = 10.0;
    private static final double STAGE_Y = 20.0;
    private static final double STAGE_WIDTH = 800.0;
    private static final double STAGE_HEIGHT = 600.0;
    private static final double STAGE_OPACITY = 0.9;

    private SampleDockingLayoutDtoFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static DockingLayoutDto createDockingLayoutDto() {
        final DockableDto dockable = new DockableDto();
        dockable.identifier = DOCKABLE_IDENTIFIER;

        final DockContainerLeafDto leaf = new DockContainerLeafDto();
        leaf.identifier = LEAF_IDENTIFIER;
        leaf.pruneWhenEmpty = true;
        leaf.selectedDockableIdentifier = DOCKABLE_IDENTIFIER;
        leaf.side = TOP;
        leaf.isResizableWithParent = true;
        leaf.isCanSplit = true;
        leaf.uncollapsedSizePx = LEAF_UNCOLLAPSED_SIZE_PX;
        leaf.isCollapsed = false;
        leaf.dockables.add(dockable);

        final DividerPositionDto divider = new DividerPositionDto();
        divider.index = DIVIDER_INDEX;
        divider.position = DIVIDER_POSITION;

        final DockContainerBranchDto branch = new DockContainerBranchDto();
        branch.identifier = BRANCH_IDENTIFIER;
        branch.pruneWhenEmpty = false;
        branch.orientation = HORIZONTAL;
        branch.dividerPositions.add(divider);
        branch.children.add(leaf);

        final DockContainerRootBranchDto root = new DockContainerRootBranchDto();
        root.identifier = ROOT_IDENTIFIER;
        root.pruneWhenEmpty = false;
        root.orientation = VERTICAL;
        root.dividerPositions.add(divider);
        root.branches.add(branch);
        root.leaf = leaf;

        final DragDropStageDto stage = new DragDropStageDto();
        stage.title = STAGE_TITLE;
        stage.x = STAGE_X;
        stage.y = STAGE_Y;
        stage.width = STAGE_WIDTH;
        stage.height = STAGE_HEIGHT;
        stage.modality = NONE;
        stage.opacity = STAGE_OPACITY;
        stage.iconified = false;
        stage.fullScreen = false;
        stage.maximized = true;
        stage.alwaysOnTop = false;
        stage.resizable = true;
        stage.showing = true;
        stage.focused = true;
        stage.autoCloseWhenEmpty = true;
        stage.dockContainerRootBranchDto = root;

        final BentoStateDto bento = new BentoStateDto();
        bento.identifier = BENTO_IDENTIFIER;
        bento.rootBranches.add(root);
        bento.dragDropStages.add(stage);

        final LayoutMetadataDto metadata = new LayoutMetadataDto();
        metadata.schemaVersion = DockingLayoutDto.getCurrentSchemaVersion();

        final DockingLayoutDto layout = new DockingLayoutDto();
        layout.metadata = metadata;
        layout.bentoStates.add(bento);

        return layout;
    }
}
