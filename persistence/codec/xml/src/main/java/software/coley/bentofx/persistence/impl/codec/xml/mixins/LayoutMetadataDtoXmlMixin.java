package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.CODEC_IDENTIFIER_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.SCHEMA_VERSION_ELEMENT_NAME;

/**
 * Jackson XML mix-in for {@code LayoutMetadataDto}.
 *
 * @author Phil Bryant
 */
public abstract class LayoutMetadataDtoXmlMixin {

    @JacksonXmlProperty(localName = SCHEMA_VERSION_ELEMENT_NAME)
    public Integer schemaVersion;

    @JacksonXmlProperty(localName = CODEC_IDENTIFIER_ELEMENT_NAME)
    public String codecIdentifier;
}
