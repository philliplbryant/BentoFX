/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.codec.BentoState;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * JSON implementation of {@link LayoutCodec}.
 */
public final class JsonLayoutCodec implements LayoutCodec {

    private final ObjectMapper mapper;

    public JsonLayoutCodec() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public @NotNull BentoState newBentoState() {
        return new BentoState.BentoStateBuilder("bento").build();
    }

    @Override
    public void encode(
            final @NotNull BentoState state,
            final @NotNull OutputStream outputStream
    ) throws BentoStateException {
        try {
            final BentoStateDto dto = BentoStateMapper.toDto(state);
            mapper.writeValue(outputStream, dto);
        } catch (final Exception e) {
            throw new BentoStateException("Failed to encode BentoState as JSON", e);
        }
    }

    @Override
    public @NotNull BentoState decode(
            @NotNull
            final InputStream inputStream
    ) throws BentoStateException {
        try {
            final BentoStateDto dto = mapper.readValue(inputStream, BentoStateDto.class);
            return BentoStateMapper.fromDto(dto);
        } catch (final IOException e) {
            throw new BentoStateException("Failed to decode BentoState from JSON", e);
        }
    }
}
