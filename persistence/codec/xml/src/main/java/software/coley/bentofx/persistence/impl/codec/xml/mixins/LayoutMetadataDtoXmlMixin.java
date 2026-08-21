package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.jspecify.annotations.Nullable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DISPLAY_NAME_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.SCHEMA_VERSION_ELEMENT_NAME;

/**
 * Jackson XML mix-in for {@code LayoutMetadataDto}.
 *
 * <p>{@code NON_NULL} is what keeps an unnamed layout from writing an empty
 * {@code <displayName/>}, which XML reads back as {@code ""} rather than
 * {@code null}.</p>
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
abstract class LayoutMetadataDtoXmlMixin {

    @JacksonXmlProperty(localName = SCHEMA_VERSION_ELEMENT_NAME)
    public @Nullable Integer schemaVersion;

    @JacksonXmlProperty(localName = DISPLAY_NAME_ELEMENT_NAME)
    public @Nullable String displayName;
}
