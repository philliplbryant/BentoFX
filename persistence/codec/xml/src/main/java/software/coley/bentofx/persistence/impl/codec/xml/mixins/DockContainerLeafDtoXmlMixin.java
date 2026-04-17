package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockableDto;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DOCKABLE_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DOCKABLE_LIST_ELEMENT_NAME;

/**
 * Jackson XML mix-in for {@code DockContainerLeafDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
public abstract class DockContainerLeafDtoXmlMixin {

    @JacksonXmlElementWrapper(localName = DOCKABLE_LIST_ELEMENT_NAME)
    @JacksonXmlProperty(localName = DOCKABLE_ELEMENT_NAME)
    public List<DockableDto> dockables;

    @JacksonXmlProperty(isAttribute = true)
    public String identifier;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean pruneWhenEmpty;

    @JacksonXmlProperty(isAttribute = true)
    public String selectedDockableIdentifier;

    @JacksonXmlProperty(isAttribute = true)
    public javafx.geometry.Side side;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean isResizableWithParent;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean isCanSplit;

    @JacksonXmlProperty(isAttribute = true)
    public Double uncollapsedSizePx;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean isCollapsed;
}
