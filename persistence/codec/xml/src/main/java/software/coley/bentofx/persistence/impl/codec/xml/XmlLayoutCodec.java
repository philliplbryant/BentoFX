package software.coley.bentofx.persistence.impl.codec.xml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;
import software.coley.bentofx.persistence.impl.codec.xml.mixins.XmlMapperMixins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * XML implementation of {@link LayoutCodec} using Jackson XML and external
 * mix-ins.
 *
 * @author Phil Bryant
 */
public final class XmlLayoutCodec implements LayoutCodec {

    public static final String EXTENSION = "xml";

    private final XmlMapper mapper;

    public XmlLayoutCodec() {
        this.mapper = (XmlMapper) XmlMapperMixins.registerAll(
                XmlMapper.builder()
                        .enable(SerializationFeature.INDENT_OUTPUT)
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build()
        );
    }

    @Override
    public String getIdentifier() {
        return EXTENSION;
    }

    @Override
    public void encode(
            final List<BentoState> bentoStates,
            final OutputStream outputStream
    ) throws BentoStateException {
        try {
            final DockingLayoutDto dto = BentoStateMapper.toDto(bentoStates);
            mapper.writeValue(outputStream, dto);
        } catch (final Exception e) {
            throw new BentoStateException("Failed to encode BentoState as XML", e);
        }
    }

    @Override
    public List<BentoState> decode(
            final InputStream inputStream
    ) throws BentoStateException {
        try {
            final DockingLayoutDto dockingLayoutDto =
                    mapper.readValue(inputStream, DockingLayoutDto.class);

            return BentoStateMapper.fromDto(dockingLayoutDto);
        } catch (final IOException e) {
            throw new BentoStateException("Failed to decode BentoStateList from XML", e);
        }
    }
}
