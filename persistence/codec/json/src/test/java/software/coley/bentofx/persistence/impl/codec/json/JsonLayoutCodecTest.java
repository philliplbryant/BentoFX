package software.coley.bentofx.persistence.impl.codec.json;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.LayoutMetadataDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .describedAs("encoded JSON schema version")
                .contains("\"schemaVersion\" : " + DockingLayoutDto.getCurrentSchemaVersion());
        assertThat(json)
                .describedAs("encoded JSON codec identifier")
                .contains("\"codecIdentifier\" : \"json\"");
    }

    @Test
    void decodeRejectsFutureSchemaVersion() {
        final JsonLayoutCodec codec = new JsonLayoutCodec();
        final int futureSchemaVersion =
                DockingLayoutDto.getCurrentSchemaVersion() + 1;
        final String json = """
                {
                  "metadata": {
                    "schemaVersion": %d,
                    "codecIdentifier": "json"
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

    private static List<BentoState> createStates() throws BentoStateException {
        final DockingLayoutDto layout = new DockingLayoutDto();

        final LayoutMetadataDto metadata = new LayoutMetadataDto();
        metadata.schemaVersion = DockingLayoutDto.getCurrentSchemaVersion();
        layout.metadata = metadata;

        final BentoStateDto bento = new BentoStateDto();
        bento.identifier = "bento-1";
        layout.bentoStates.add(bento);

        return BentoStateMapper.fromDto(layout);
    }
}
