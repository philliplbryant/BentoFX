package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.BENTO_LIST_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DOCKING_LAYOUT_ROOT_ELEMENT_NAME;

/**
 * Jackson JSON mix-in for {@code DockingLayoutDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
@JsonRootName(DOCKING_LAYOUT_ROOT_ELEMENT_NAME)
public abstract class DockingLayoutDtoJsonMixin {

    @JsonProperty(BENTO_LIST_ELEMENT_NAME)
    public List<BentoStateDto> bentoStates;
}
