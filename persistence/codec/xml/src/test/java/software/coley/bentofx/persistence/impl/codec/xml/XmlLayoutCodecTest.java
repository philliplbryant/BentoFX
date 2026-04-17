package software.coley.bentofx.persistence.impl.codec.xml;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.impl.codec.BentoState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

class XmlLayoutCodecTest {

    @Test
    void getIdentifierReturnsXml() {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        assertEquals("xml", codec.getIdentifier());
    }

    @Test
    void encodeProducesExpectedElementNames() throws Exception {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        final List<BentoState> states = createStates();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(states, out);

        final String xml = out.toString(StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(xml.contains("<" + DOCKING_LAYOUT_ROOT_ELEMENT_NAME + ">")),
                () -> assertTrue(xml.contains("<" + BENTO_LIST_ELEMENT_NAME + ">")),
                () -> assertTrue(xml.contains("<" + BENTO_ELEMENT_NAME)),
                () -> assertTrue(xml.contains("<" + ROOT_BRANCH_LIST_ELEMENT_NAME + ">")),
                () -> assertTrue(xml.contains("<" + ROOT_BRANCH_ELEMENT_NAME)),
                () -> assertTrue(xml.contains("<" + DIVIDER_POSITION_LIST_ELEMENT_NAME + ">")),
                () -> assertTrue(xml.contains("<" + DIVIDER_ELEMENT_NAME)),
                () -> assertTrue(xml.contains("<" + BRANCH_LIST_ELEMENT_NAME + ">")),
                () -> assertTrue(xml.contains("<" + BRANCH_ELEMENT_NAME)),
                () -> assertTrue(xml.contains("<" + LEAF_ELEMENT_NAME)),
                () -> assertTrue(xml.contains("<" + DOCKABLE_LIST_ELEMENT_NAME + ">")),
                () -> assertTrue(xml.contains("<" + DOCKABLE_ELEMENT_NAME)),
                () -> assertTrue(xml.contains("<" + DRAG_DROP_STAGE_LIST_ELEMENT_NAME + ">")),
                () -> assertTrue(xml.contains("<" + DRAG_DROP_STAGE_ELEMENT_NAME))
        );
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

        assertAll(
                () -> assertEquals(originalDto.bentoStates.size(), restoredDto.bentoStates.size()),
                () -> assertEquals(originalDto.bentoStates.get(0).identifier, restoredDto.bentoStates.get(0).identifier),
                () -> assertEquals(originalDto.bentoStates.get(0).rootBranches.get(0).identifier, restoredDto.bentoStates.get(0).rootBranches.get(0).identifier),
                () -> assertEquals(originalDto.bentoStates.get(0).dragDropStages.get(0).title, restoredDto.bentoStates.get(0).dragDropStages.get(0).title)
        );
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
