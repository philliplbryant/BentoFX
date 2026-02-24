package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Mappable Data Transfer Object representing the state of a BentoFX layout.
 *
 * @author Phil Bryant
 */
@XmlRootElement(name = DOCKING_LAYOUT_ROOT_ELEMENT_NAME)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BentoStateListDto {

    private final List<BentoStateDto> bentoStates = new ArrayList<>();

    @XmlElementWrapper(name = BENTO_LIST_ELEMENT_NAME)
    @XmlElement(name = BENTO_ELEMENT_NAME)
    @JsonProperty(BENTO_ELEMENT_NAME)
    public List<BentoStateDto> getBentoStates() {
        return bentoStates;
    }
}
