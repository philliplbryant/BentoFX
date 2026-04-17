package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Jackson XML mix-in for {@code DividerPositionDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
public abstract class DividerPositionDtoXmlMixin {

    @JacksonXmlProperty(isAttribute = true)
    public Integer index;

    @JacksonXmlProperty(isAttribute = true)
    public Double position;
}
