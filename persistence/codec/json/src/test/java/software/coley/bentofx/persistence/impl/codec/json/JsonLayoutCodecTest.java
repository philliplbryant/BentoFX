package software.coley.bentofx.persistence.impl.codec.json;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static software.coley.bentofx.persistence.testfixtures.codec.dto.SampleDockingLayoutDtoFactory.createDockingLayoutDto;
import static software.coley.bentofx.persistence.testfixtures.codec.state.SampleBentoStateFactory.createBentoStates;

final class JsonLayoutCodecTest {

    @Test
    void getIdentifierReturnsJson() {
        final JsonLayoutCodec codec = new JsonLayoutCodec();

        assertThat(codec.getIdentifier())
                .describedAs("codec identifier")
                .isEqualTo("json");
    }

    @Test
    void encodeWritesLayoutMetadata() throws Exception {
        final JsonLayoutCodec codec = new JsonLayoutCodec();
        final List<BentoState> states = createStates();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(states, out);

        final String json = out.toString(StandardCharsets.UTF_8);

        assertThat(json)
                .describedAs("encoded JSON schema version metadata")
                .contains("\"schemaVersion\" : " + DockingLayoutDto.getCurrentSchemaVersion());
    }

    @Test
    void decodeRejectsFutureSchemaVersion() {
        final JsonLayoutCodec codec = new JsonLayoutCodec();
        final int futureSchemaVersion =
                DockingLayoutDto.getCurrentSchemaVersion() + 1;
        final String json = """
                {
                  "metadata": {
                    "schemaVersion": %d
                  },
                  "bentos": []
                }
                """.formatted(futureSchemaVersion);

        assertThatThrownBy(() ->
                codec.decode(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
        )
                .describedAs("future JSON schema version validation")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Unsupported BentoFX docking layout schema version");
    }

    @Test
    void decodeReportsMissingBentoIdentifierAsBentoStateException() {
        final JsonLayoutCodec codec = new JsonLayoutCodec();
        final String json = """
                {
                  "metadata": {
                    "schemaVersion": %d
                  },
                  "bentos": [ { "rootBranches": [] } ]
                }
                """.formatted(DockingLayoutDto.getCurrentSchemaVersion());

        assertThatThrownBy(() ->
                codec.decode(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
        )
                .describedAs("missing Bento identifier validation")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("no identifier");
    }

    @Test
    void decodeWrapsAnUncheckedFailureAsBentoStateException() {
        final JsonLayoutCodec codec = new JsonLayoutCodec();

        // A null Bento list, which Jackson maps straight onto the DTO field and
        // the mapper then iterates.
        final String json = """
                {
                  "metadata": {
                    "schemaVersion": %d
                  },
                  "bentos": null
                }
                """.formatted(DockingLayoutDto.getCurrentSchemaVersion());

        assertThatThrownBy(() ->
                codec.decode(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
        )
                .describedAs("unchecked decode failure reporting")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Failed to decode BentoStateList from JSON")
                .cause()
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void encodeThenDecodeRoundTripsTheWholeLayout() throws Exception {
        final JsonLayoutCodec codec = new JsonLayoutCodec();
        final List<BentoState> original = createBentoStates();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(original, out);

        final List<BentoState> restored = codec.decode(
                new ByteArrayInputStream(out.toByteArray())
        );

        assertThat(restored)
                .describedAs("layout restored from JSON")
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void encodeThenDecodePreservesMixedRootChildOrder() throws Exception {
        final JsonLayoutCodec codec = new JsonLayoutCodec();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.encode(mixedRootStates(), out);

        final List<BentoState> restored = codec.decode(
                new ByteArrayInputStream(out.toByteArray())
        );

        assertThat(restored.getFirst()
                .getRootBranchStates().getFirst()
                .getChildDockContainerStates())
                .describedAs("decoded root branch children, in order")
                .extracting(DockContainerState::getIdentifier)
                .containsExactly("leaf-A", "branch-B", "leaf-C");
    }

    private static List<BentoState> createStates() throws BentoStateException {
        return BentoStateMapper.fromDto(createDockingLayoutDto());
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
