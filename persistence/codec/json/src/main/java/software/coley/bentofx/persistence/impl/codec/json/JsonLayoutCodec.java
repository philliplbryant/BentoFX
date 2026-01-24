/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.codec.BentoState;
import software.coley.bentofx.persistence.api.codec.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * JSON implementation of {@link LayoutCodec}.
 *
 * @author Phil Bryant
 */
public final class JsonLayoutCodec implements LayoutCodec {

    public static final String EXTENSION = "json";
    private final ObjectMapper mapper;

    public JsonLayoutCodec() {
        this.mapper = new ObjectMapper()
                .enable(INDENT_OUTPUT)
                .disable(FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public @NotNull BentoState newBentoState() {
        return new BentoStateBuilder().build();
    }

    @Override
    public String getIdentifier() {
        return EXTENSION;
    }

    @Override
    public void encode(
            final @NotNull BentoState state,
            final @NotNull OutputStream outputStream
    ) throws BentoStateException {

        try {

            final BentoStateDto bentoStateDto = BentoStateMapper.toDto(state);
            mapper.writeValue(outputStream, bentoStateDto);
        } catch (final Exception e) {

            throw new BentoStateException(
                    "Failed to encode BentoState as JSON",
                    e
            );
        }
    }

    @Override
    public @NotNull BentoState decode(
            @NotNull
            final InputStream inputStream
    ) throws BentoStateException {
        try {
            final BentoStateDto bentoStateDto =
                    mapper.readValue(
                            inputStream,
                            BentoStateDto.class
                    );

            return BentoStateMapper.fromDto(bentoStateDto);
        } catch (final IOException e) {

            throw new BentoStateException(
                    "Failed to decode BentoState from JSON",
                    e
            );
        }
    }
}
