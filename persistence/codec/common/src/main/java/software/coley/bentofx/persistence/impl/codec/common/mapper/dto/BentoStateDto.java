/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Mappable Data Transfer Object representing the state of a BentoFX layout.
 *
 * @author Phil Bryant
 */
@XmlRootElement(name = BENTO_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BentoStateDto {

    @XmlElementWrapper(name = ROOT_BRANCH_LIST_ELEMENT_NAME)
    @XmlElement(name = ROOT_BRANCH_ELEMENT_NAME)
    @JsonProperty(ROOT_BRANCH_ELEMENT_NAME)
    public List<DockContainerRootBranchDto> rootBranches = new ArrayList<>();
}
