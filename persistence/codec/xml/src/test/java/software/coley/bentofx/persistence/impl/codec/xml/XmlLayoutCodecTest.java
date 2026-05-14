package software.coley.bentofx.persistence.impl.codec.xml;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockableDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DragDropStageDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

class XmlLayoutCodecTest {

    @Test
    void getIdentifierReturnsXml() {
        final XmlLayoutCodec codec = new XmlLayoutCodec();

        assertThat(codec.getIdentifier()).isEqualTo("xml");
    }

    @Test
    void encodeProducesExpectedElementNames() throws Exception {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        final List<BentoState> states = createStates();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(states, out);

        final String xml = out.toString(StandardCharsets.UTF_8);

        assertThat(xml)
                .contains("<" + DOCKING_LAYOUT_ROOT_ELEMENT_NAME + ">")
                .contains("<" + BENTO_LIST_ELEMENT_NAME + ">")
                .contains("<" + BENTO_ELEMENT_NAME)
                .contains("<" + ROOT_BRANCH_LIST_ELEMENT_NAME + ">")
                .contains("<" + ROOT_BRANCH_ELEMENT_NAME)
                .contains("<" + DIVIDER_POSITION_LIST_ELEMENT_NAME + ">")
                .contains("<" + DIVIDER_ELEMENT_NAME)
                .contains("<" + BRANCH_LIST_ELEMENT_NAME + ">")
                .contains("<" + BRANCH_ELEMENT_NAME)
                .contains("<" + LEAF_ELEMENT_NAME)
                .contains("<" + DOCKABLE_LIST_ELEMENT_NAME + ">")
                .contains("<" + DOCKABLE_ELEMENT_NAME)
                .contains("<" + DRAG_DROP_STAGE_LIST_ELEMENT_NAME + ">")
                .contains("<" + DRAG_DROP_STAGE_ELEMENT_NAME);
    }

    @Test
    void encodeThenDecodeRoundTripsThroughCommonMapper() throws Exception {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        final List<BentoState> original = createStates();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(original, out);

        final List<BentoState> restored = codec.decode(
                new ByteArrayInputStream(out.toByteArray())
        );

        final DockingLayoutDto originalDto = BentoStateMapper.toDto(original);
        final DockingLayoutDto restoredDto = BentoStateMapper.toDto(restored);

        assertThat(restoredDto.bentoStates)
                .hasSize(originalDto.bentoStates.size());

        assertThat(restoredDto.bentoStates.getFirst().identifier)
                .isEqualTo(originalDto.bentoStates.getFirst().identifier);

        assertThat(restoredDto.bentoStates.getFirst().rootBranches.getFirst().identifier)
                .isEqualTo(originalDto.bentoStates.getFirst().rootBranches.getFirst().identifier);

        assertThat(restoredDto.bentoStates.getFirst().dragDropStages.getFirst().title)
                .isEqualTo(originalDto.bentoStates.getFirst().dragDropStages.getFirst().title);
    }

    private static List<BentoState> createStates() {
        return BentoStateMapper.fromDto(createDockingLayoutDto());
    }

    private static DockingLayoutDto createDockingLayoutDto() {
        final DockableDto dockable = new DockableDto();
        dockable.identifier = "dockable-1";

        final DockContainerLeafDto leaf = new DockContainerLeafDto();
        leaf.identifier = "leaf-1";
        leaf.pruneWhenEmpty = true;
        leaf.selectedDockableIdentifier = "dockable-1";
        leaf.side = javafx.geometry.Side.TOP;
        leaf.isResizableWithParent = true;
        leaf.isCanSplit = true;
        leaf.uncollapsedSizePx = 321.0;
        leaf.isCollapsed = false;
        leaf.dockables.add(dockable);

        final DividerPositionDto divider = new DividerPositionDto();
        divider.index = 0;
        divider.position = 0.42;

        final DockContainerBranchDto branch = new DockContainerBranchDto();
        branch.identifier = "branch-1";
        branch.pruneWhenEmpty = false;
        branch.orientation = javafx.geometry.Orientation.HORIZONTAL;
        branch.dividerPositions.add(divider);
        branch.children.add(leaf);

        final DockContainerRootBranchDto root = new DockContainerRootBranchDto();
        root.identifier = "root-1";
        root.pruneWhenEmpty = false;
        root.orientation = javafx.geometry.Orientation.VERTICAL;
        root.dividerPositions.add(divider);
        root.branches.add(branch);
        root.leaf = leaf;

        final DragDropStageDto stage = new DragDropStageDto();
        stage.title = "Stage";
        stage.x = 10.0;
        stage.y = 20.0;
        stage.width = 800.0;
        stage.height = 600.0;
        stage.modality = javafx.stage.Modality.NONE;
        stage.opacity = 0.9;
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
        bento.identifier = "bento-1";
        bento.rootBranches.add(root);
        bento.dragDropStages.add(stage);

        final DockingLayoutDto layout = new DockingLayoutDto();
        layout.bentoStates.add(bento);

        return layout;
    }
}
