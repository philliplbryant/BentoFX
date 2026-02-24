/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.*;
import javafx.geometry.Orientation;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainerRootBranch}.
 *
 * @author Phil Bryant
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DockContainerRootBranchDto {

    @XmlAttribute
    public String identifier;

    @XmlAttribute
    public Boolean pruneWhenEmpty;

    @XmlAttribute
    public Orientation orientation;

    @XmlElement(name =  PARENT_STAGE_ELEMENT_NAME)
    @JsonProperty(PARENT_STAGE_ELEMENT_NAME)
    public IdentifiableStageDto parentStage;

    @XmlElementWrapper(name = DIVIDER_POSITION_LIST_ELEMENT_NAME)
    @XmlElement(name = DIVIDER_ELEMENT_NAME)
    @JsonProperty(DIVIDER_ELEMENT_NAME)
    public List<DividerPositionDto> dividerPositions = new ArrayList<>();

    @XmlElementWrapper(name = BRANCH_LIST_ELEMENT_NAME)
    @XmlElement(name = BRANCH_ELEMENT_NAME)
    @JsonProperty(BRANCH_ELEMENT_NAME)
    public List<DockContainerBranchDto> branches = new ArrayList<>();

    @XmlElement(name = LEAF_ELEMENT_NAME)
    @JsonProperty(LEAF_ELEMENT_NAME)
    public DockContainerLeafDto leaf;
}
