package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DISPLAY_NAME_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.SCHEMA_VERSION_ELEMENT_NAME;

/**
 * Jackson JSON mix-in for {@code LayoutMetadataDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
abstract class LayoutMetadataDtoJsonMixin {

    @JsonProperty(SCHEMA_VERSION_ELEMENT_NAME)
    public @Nullable Integer schemaVersion;

    @JsonProperty(DISPLAY_NAME_ELEMENT_NAME)
    public @Nullable String displayName;
}
