package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Jackson XML mix-in for {@code DockingLayoutDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
@JsonRootName(DOCKING_LAYOUT_ROOT_ELEMENT_NAME)
@JacksonXmlRootElement(localName = DOCKING_LAYOUT_ROOT_ELEMENT_NAME)
public abstract class DockingLayoutDtoXmlMixin {

    @JacksonXmlElementWrapper(localName = BENTO_LIST_ELEMENT_NAME)
    @JacksonXmlProperty(localName = BENTO_ELEMENT_NAME)
    public List<BentoStateDto> bentoStates;
}
