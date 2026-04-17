package software.coley.bentofx.persistence.impl.codec.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.BentoState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static software.coley.bentofx.persistence.impl.codec.json.mixins.ObjectMapperMixins.registerAll;

/**
 * JSON implementation of {@link LayoutCodec}.
 *
 * @author Phil Bryant
 */
public final class JsonLayoutCodec implements LayoutCodec {

    public static final String EXTENSION = "json";
    private final ObjectMapper mapper;

    public JsonLayoutCodec() {
        this.mapper = registerAll(
                new ObjectMapper()
                        .enable(INDENT_OUTPUT)
                        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        );
    }

    @Override
    public String getIdentifier() {
        return EXTENSION;
    }

    @Override
    public void encode(
            final List<BentoState> states,
            final OutputStream outputStream
    ) throws BentoStateException {

        try {

            final DockingLayoutDto bentoStateDto = BentoStateMapper.toDto(states);
            mapper.writeValue(outputStream, bentoStateDto);
        } catch (final Exception e) {

            throw new BentoStateException(
                    "Failed to encode BentoState as JSON",
                    e
            );
        }
    }

    @Override
    public List<BentoState> decode(
            final InputStream inputStream
    ) throws BentoStateException {
        try {
            final DockingLayoutDto dockingLayoutDto =
                    mapper.readValue(
                            inputStream,
                            DockingLayoutDto.class
                    );

            return BentoStateMapper.fromDto(dockingLayoutDto);
        } catch (final IOException e) {

            throw new BentoStateException(
                    "Failed to decode BentoStateList from JSON",
                    e
            );
        }
    }
}
