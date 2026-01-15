/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DockContainerRootBranchDto {

    @XmlAttribute
    public String identifier;

    @XmlElement(name = "parentStage")
    public DragDropStageDto parentStage;

    @XmlElementWrapper(name = "branches")
    @XmlElement(name = "branch")
    public List<DockContainerBranchDto> branches = new ArrayList<>();

    @XmlElementWrapper(name = "leaves")
    @XmlElement(name = "leaf")
    public List<DockContainerLeafDto> leaves = new ArrayList<>();
}
