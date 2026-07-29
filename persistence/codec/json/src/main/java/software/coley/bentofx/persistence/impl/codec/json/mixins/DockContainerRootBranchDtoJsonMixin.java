package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DIVIDER_POSITION_LIST_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.BRANCH_LIST_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.BRANCH_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.LEAF_ELEMENT_NAME;

/**
 * Jackson JSON mix-in for {@code DockContainerRootBranchDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
@JsonTypeName(BRANCH_ELEMENT_NAME)
public abstract class DockContainerRootBranchDtoJsonMixin {

    @JsonProperty(DIVIDER_POSITION_LIST_ELEMENT_NAME)
    public @Nullable List<DividerPositionDto> dividerPositions;

    @JsonProperty(BRANCH_LIST_ELEMENT_NAME)
    public @Nullable List<DockContainerBranchDto> branches;

    @JsonProperty(LEAF_ELEMENT_NAME)
    public @Nullable DockContainerLeafDto leaf;
}
