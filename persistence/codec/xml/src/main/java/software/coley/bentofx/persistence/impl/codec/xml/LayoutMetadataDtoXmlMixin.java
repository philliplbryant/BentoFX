package software.coley.bentofx.persistence.impl.codec.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.jspecify.annotations.Nullable;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.SCHEMA_VERSION_ELEMENT_NAME;

/**
 * Jackson XML mix-in for {@code LayoutMetadataDto}.
 *
 * @author Phil Bryant
 */
abstract class LayoutMetadataDtoXmlMixin {

    @JacksonXmlProperty(localName = SCHEMA_VERSION_ELEMENT_NAME)
    public @Nullable Integer schemaVersion;
}
