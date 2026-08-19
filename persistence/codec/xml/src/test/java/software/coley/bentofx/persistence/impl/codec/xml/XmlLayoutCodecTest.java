package software.coley.bentofx.persistence.impl.codec.xml;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.core.api.BentoStateException;
import software.coley.bentofx.persistence.core.api.codec.PersistableLayout;
import software.coley.bentofx.persistence.core.api.state.BentoState;
import software.coley.bentofx.persistence.core.api.state.DockContainerBranchState;
import software.coley.bentofx.persistence.core.api.state.DockContainerLeafState;
import software.coley.bentofx.persistence.core.api.state.DockContainerRootBranchState;
import software.coley.bentofx.persistence.core.api.state.DockContainerState;
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
import static software.coley.bentofx.persistence.testfixtures.codec.state.SampleBentoStateFactory.createBentoStates;

class XmlLayoutCodecTest {

    private static final String XML_CODEC_IDENTIFIER = "xml";
    private static final String OPENING_TAG_PREFIX = "<";
    private static final String CLOSING_TAG_SUFFIX = ">";
    private static final String ATTRIBUTE_SEPARATOR = " ";

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
        codec.encode(PersistableLayout.of(states), out);

        final String xml = out.toString(StandardCharsets.UTF_8);

        assertThat(xml)
                .describedAs("encoded XML element names")
                .contains(element(DOCKING_LAYOUT_ROOT_ELEMENT_NAME))
                .contains(element(METADATA_ELEMENT_NAME))
                .contains(element(SCHEMA_VERSION_ELEMENT_NAME))
                .contains(element(BENTO_LIST_ELEMENT_NAME))
                .contains(elementWithAttributes(BENTO_ELEMENT_NAME))
                .contains(element(ROOT_BRANCH_LIST_ELEMENT_NAME))
                .contains(elementWithAttributes(ROOT_BRANCH_ELEMENT_NAME))
                .contains(element(DIVIDER_POSITION_LIST_ELEMENT_NAME))
                .contains(elementWithAttributes(DIVIDER_ELEMENT_NAME))
                .contains(element(CHILD_DOCK_CONTAINER_LIST_ELEMENT_NAME))
                .contains(elementWithAttributes(BRANCH_ELEMENT_NAME))
                .contains(elementWithAttributes(LEAF_ELEMENT_NAME))
                .contains(element(DOCKABLE_LIST_ELEMENT_NAME))
                .contains(elementWithAttributes(DOCKABLE_ELEMENT_NAME))
                .contains(element(DRAG_DROP_STAGE_LIST_ELEMENT_NAME))
                .contains(elementWithAttributes(DRAG_DROP_STAGE_ELEMENT_NAME));
    }

    /**
     * {@return the opening tag of an element that carries no attributes.}
     *
     * @param elementName the element's name.
     */
    private static String element(final String elementName) {
        return OPENING_TAG_PREFIX + elementName + CLOSING_TAG_SUFFIX;
    }

    /**
     * {@return the start of the opening tag of an element that carries
     * attributes, up to and including the space before the first one.}
     *
     * <p>The trailing space is what makes this an element name rather than a
     * prefix of one: {@code <branch} alone also matches {@code <branches>}.</p>
     *
     * @param elementName the element's name.
     */
    private static String elementWithAttributes(final String elementName) {
        return OPENING_TAG_PREFIX + elementName + ATTRIBUTE_SEPARATOR;
    }

    @Test
    void decodeRejectsAnUnrecognizedProperty() {
        final XmlLayoutCodec codec = new XmlLayoutCodec();

        final String xml = """
                <dockingLayout>
                  <metadata>
                    <schemaVersion>%d</schemaVersion>
                  </metadata>
                  <bentos>
                    <bento identifier="bento-1">
                      <unexpected>true</unexpected>
                    </bento>
                  </bentos>
                </dockingLayout>
                """.formatted(DockingLayoutDto.getCurrentSchemaVersion());

        assertThatThrownBy(() ->
                codec.decode(new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)))
                        .bentoStates()
        )
                .describedAs("unrecognized XML element reporting")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Failed to decode layout from XML")
                .cause()
                .hasMessageContaining("unexpected");
    }

    @Test
    void encodeThenDecodeRoundTripsTheWholeLayout() throws Exception {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        final List<BentoState> original = createBentoStates();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(PersistableLayout.of(original), out);

        final List<BentoState> restored = codec.decode(
                new ByteArrayInputStream(out.toByteArray())
        ).bentoStates();

        assertThat(restored)
                .describedAs("layout restored from XML")
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void encodeWritesLayoutMetadata() throws Exception {
        final XmlLayoutCodec codec = new XmlLayoutCodec();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(PersistableLayout.of(createStates()), out);

        assertThat(out.toString(StandardCharsets.UTF_8))
                .describedAs("encoded XML schema version metadata")
                .contains(
                        OPENING_TAG_PREFIX + SCHEMA_VERSION_ELEMENT_NAME
                                + CLOSING_TAG_SUFFIX
                                + DockingLayoutDto.getCurrentSchemaVersion()
                );
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
                codec.decode(new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)))
                        .bentoStates()
        )
                .describedAs("future XML schema version validation")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Unsupported BentoFX docking layout schema version");
    }

    @Test
    void decodeReportsMissingBentoIdentifierAsBentoStateException() {
        final XmlLayoutCodec codec = new XmlLayoutCodec();
        final String xml = """
                <dockingLayout>
                  <metadata>
                    <schemaVersion>%d</schemaVersion>
                  </metadata>
                  <bentos>
                    <bento/>
                  </bentos>
                </dockingLayout>
                """.formatted(DockingLayoutDto.getCurrentSchemaVersion());

        assertThatThrownBy(() ->
                codec.decode(new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)))
                        .bentoStates()
        )
                .describedAs("missing Bento identifier validation")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("no identifier");
    }

    @Test
    void encodeThenDecodePreservesMixedRootChildOrder() throws Exception {
        final XmlLayoutCodec codec = new XmlLayoutCodec();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(PersistableLayout.of(mixedRootStates()), out);

        final List<BentoState> restored = codec.decode(
                new ByteArrayInputStream(out.toByteArray())
        ).bentoStates();

        assertThat(restored.getFirst()
                .getRootBranchStates().getFirst()
                .getChildDockContainerStates())
                .describedAs("decoded root branch child dock containers, in order")
                .extracting(DockContainerState::getIdentifier)
                .containsExactly("leaf-A", "branch-B", "leaf-C");
    }

    private static List<BentoState> createStates() throws Exception {
        return BentoStateMapper.fromDto(createDockingLayoutDto()).bentoStates();
    }

    /**
     * {@return one Bento whose root branch holds a leaf, a branch, and a second
     * leaf, in that order.}
     */
    private static List<BentoState> mixedRootStates() {
        final DockContainerRootBranchState rootState =
                new DockContainerRootBranchState.DockContainerRootBranchStateBuilder("root-1")
                        .addDockContainerState(
                                new DockContainerLeafState.DockContainerLeafStateBuilder("leaf-A").build())
                        .addDockContainerState(
                                new DockContainerBranchState.DockContainerBranchStateBuilder("branch-B").build())
                        .addDockContainerState(
                                new DockContainerLeafState.DockContainerLeafStateBuilder("leaf-C").build())
                        .build();

        return List.of(
                new BentoState.BentoStateBuilder("bento-1")
                        .addRootBranchState(rootState)
                        .build()
        );
    }
}
