package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.ROOT_BRANCH_ELEMENT_NAME;

/**
 * Jackson JSON mix-in for {@code DragDropStageDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
public abstract class DragDropStageDtoJsonMixin {

    @JsonProperty(ROOT_BRANCH_ELEMENT_NAME)
    public DockContainerRootBranchDto dockContainerRootBranchDto;
}
