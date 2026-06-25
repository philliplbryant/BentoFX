package software.coley.bentofx.persistence.impl.codec.xml;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

class XmlLayoutCodecTest {

    private static final String XML_CODEC_IDENTIFIER = "xml";
    private static final String OPENING_TAG_PREFIX = "<";
    private static final String CLOSING_TAG_SUFFIX = ">";
    private static final String DOCKABLE_IDENTIFIER = "Test-Dockable";

    @Test
    void getIdentifierReturnsXml() {
        final XmlLayoutCodec codec = new XmlLayoutCodec();

        assertThat(codec.getIdentifier())
                .describedAs("codec identifier")
                .isEqualTo(XML_CODEC_IDENTIFIER);
    }

    @Test
    void encodeProducesExpectedElementNames() throws Exception {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        final List<BentoState> states = createStates();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(states, out);

        final String xml = out.toString(StandardCharsets.UTF_8);

        assertThat(xml)
                .describedAs("encoded XML element names")
                .contains(OPENING_TAG_PREFIX + DOCKING_LAYOUT_ROOT_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + METADATA_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + SCHEMA_VERSION_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + BENTO_LIST_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + BENTO_ELEMENT_NAME)
                .contains(OPENING_TAG_PREFIX + ROOT_BRANCH_LIST_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + ROOT_BRANCH_ELEMENT_NAME)
                .contains(OPENING_TAG_PREFIX + DIVIDER_POSITION_LIST_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + DIVIDER_ELEMENT_NAME)
                .contains(OPENING_TAG_PREFIX + BRANCH_LIST_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + BRANCH_ELEMENT_NAME)
                .contains(OPENING_TAG_PREFIX + LEAF_ELEMENT_NAME)
                .contains(OPENING_TAG_PREFIX + DOCKABLE_LIST_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + DOCKABLE_ELEMENT_NAME)
                .contains(OPENING_TAG_PREFIX + DRAG_DROP_STAGE_LIST_ELEMENT_NAME + CLOSING_TAG_SUFFIX)
                .contains(OPENING_TAG_PREFIX + DRAG_DROP_STAGE_ELEMENT_NAME);
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

        assertThat(restoredDto.metadata)
                .describedAs("restored layout metadata")
                .isNotNull();
        assertThat(restoredDto.metadata.schemaVersion)
                .describedAs("restored schema version")
                .isEqualTo(DockingLayoutDto.getCurrentSchemaVersion());
        assertThat(restoredDto.bentoStates)
                .describedAs("restored Bento states")
                .hasSize(originalDto.bentoStates.size());

        assertThat(restoredDto.bentoStates.getFirst().identifier)
                .describedAs("restored Bento identifier")
                .isEqualTo(originalDto.bentoStates.getFirst().identifier);

        assertThat(restoredDto.bentoStates.getFirst().rootBranches.getFirst().identifier)
                .describedAs("restored root branch identifier")
                .isEqualTo(originalDto.bentoStates.getFirst().rootBranches.getFirst().identifier);

        assertThat(restoredDto.bentoStates.getFirst().dragDropStages.getFirst().title)
                .describedAs("restored drag/drop stage title")
                .isEqualTo(originalDto.bentoStates.getFirst().dragDropStages.getFirst().title);
    }



    @Test
    void decodeRejectsFutureSchemaVersion() {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        final int futureSchemaVersion =
                DockingLayoutDto.getCurrentSchemaVersion() + 1;
        final String xml = """
                <dockingLayout>
                  <metadata>
                    <schemaVersion>%d</schemaVersion>
                  </metadata>
                  <bentos/>
                </dockingLayout>
                """.formatted(futureSchemaVersion);

        assertThatThrownBy(() ->
                codec.decode(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
        )
                .describedAs("future XML schema version validation")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Unsupported BentoFX docking layout schema version");
    }

    private static List<BentoState> createStates() throws Exception {
        return BentoStateMapper.fromDto(createDockingLayoutDto());
    }

    private static DockingLayoutDto createDockingLayoutDto() {
        final DockableDto dockable = new DockableDto();
        dockable.identifier = DOCKABLE_IDENTIFIER;

        final DockContainerLeafDto leaf = new DockContainerLeafDto();
        leaf.identifier = "leaf-1";
        leaf.pruneWhenEmpty = true;
        leaf.selectedDockableIdentifier = DOCKABLE_IDENTIFIER;
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

        final LayoutMetadataDto metadata = new LayoutMetadataDto();
        metadata.schemaVersion = DockingLayoutDto.getCurrentSchemaVersion();

        final DockingLayoutDto layout = new DockingLayoutDto();
        layout.metadata = metadata;
        layout.bentoStates.add(bento);

        return layout;
    }
}
