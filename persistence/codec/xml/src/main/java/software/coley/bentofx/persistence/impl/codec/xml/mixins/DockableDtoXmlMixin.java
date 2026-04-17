package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Jackson XML mix-in for {@code DockableDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
public abstract class DockableDtoXmlMixin {

    @JacksonXmlProperty(isAttribute = true)
    public String identifier;
}
