package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Jackson XML mix-in for {@code DockContainerRootBranchDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
public abstract class DockContainerRootBranchDtoXmlMixin {

    @JacksonXmlProperty(isAttribute = true)
    public String identifier;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean pruneWhenEmpty;

    @JacksonXmlProperty(isAttribute = true)
    public javafx.geometry.Orientation orientation;

    @JacksonXmlElementWrapper(localName = DIVIDER_POSITION_LIST_ELEMENT_NAME)
    @JacksonXmlProperty(localName = DIVIDER_ELEMENT_NAME)
    public List<DividerPositionDto> dividerPositions;

    @JacksonXmlElementWrapper(localName = BRANCH_LIST_ELEMENT_NAME)
    @JacksonXmlProperty(localName = BRANCH_ELEMENT_NAME)
    public List<DockContainerBranchDto> branches;

    @JacksonXmlProperty(localName = LEAF_ELEMENT_NAME)
    public DockContainerLeafDto leaf;
}
