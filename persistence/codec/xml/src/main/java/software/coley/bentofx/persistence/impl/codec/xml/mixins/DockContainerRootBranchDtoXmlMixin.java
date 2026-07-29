package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import javafx.geometry.Orientation;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DIVIDER_POSITION_LIST_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DIVIDER_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.BRANCH_LIST_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.BRANCH_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.LEAF_ELEMENT_NAME;

/**
 * Jackson XML mix-in for {@code DockContainerRootBranchDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
abstract class DockContainerRootBranchDtoXmlMixin {

    @JacksonXmlProperty(isAttribute = true)
    public @Nullable String identifier;

    @JacksonXmlProperty(isAttribute = true)
    public @Nullable Boolean pruneWhenEmpty;

    @JacksonXmlProperty(isAttribute = true)
    public @Nullable Orientation orientation;

    @JacksonXmlElementWrapper(localName = DIVIDER_POSITION_LIST_ELEMENT_NAME)
    @JacksonXmlProperty(localName = DIVIDER_ELEMENT_NAME)
    public @Nullable List<DividerPositionDto> dividerPositions;

    @JacksonXmlElementWrapper(localName = BRANCH_LIST_ELEMENT_NAME)
    @JacksonXmlProperty(localName = BRANCH_ELEMENT_NAME)
    public @Nullable List<DockContainerBranchDto> branches;

    @JacksonXmlProperty(localName = LEAF_ELEMENT_NAME)
    public @Nullable DockContainerLeafDto leaf;
}
