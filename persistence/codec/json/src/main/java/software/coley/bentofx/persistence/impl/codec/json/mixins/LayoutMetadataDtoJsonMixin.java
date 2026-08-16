package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.SCHEMA_VERSION_ELEMENT_NAME;

/**
 * Jackson JSON mix-in for {@code LayoutMetadataDto}.
 *
 * @author Phil Bryant
 */
abstract class LayoutMetadataDtoJsonMixin {

    @JsonProperty(SCHEMA_VERSION_ELEMENT_NAME)
    public @Nullable Integer schemaVersion;
}
