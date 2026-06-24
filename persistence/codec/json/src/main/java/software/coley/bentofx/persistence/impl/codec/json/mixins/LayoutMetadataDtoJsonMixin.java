package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.CODEC_IDENTIFIER_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.SCHEMA_VERSION_ELEMENT_NAME;

/**
 * Jackson JSON mix-in for {@code LayoutMetadataDto}.
 *
 * @author Phil Bryant
 */
public abstract class LayoutMetadataDtoJsonMixin {

    @JsonProperty(SCHEMA_VERSION_ELEMENT_NAME)
    public Integer schemaVersion;

    @JsonProperty(CODEC_IDENTIFIER_ELEMENT_NAME)
    public String codecIdentifier;
}
