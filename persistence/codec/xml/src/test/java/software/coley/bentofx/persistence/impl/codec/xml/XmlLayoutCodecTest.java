package software.coley.bentofx.persistence.impl.codec.xml;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;
import static software.coley.bentofx.persistence.testfixtures.codec.dto.SampleDockingLayoutDtoFactory.createDockingLayoutDto;

class XmlLayoutCodecTest {

    private static final String XML_CODEC_IDENTIFIER = "xml";
    private static final String OPENING_TAG_PREFIX = "<";
    private static final String CLOSING_TAG_SUFFIX = ">";

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
}
