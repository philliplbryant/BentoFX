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
import jakarta.xml.bind.annotation.XmlElements;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DockContainerBranchDto extends DockContainerDto {

    @XmlAttribute
    public String orientation; // "HORIZONTAL" or "VERTICAL"

    @XmlElementWrapper(name = "dividerPositions")
    @XmlElement(name = "divider")
    public List<DividerPositionDto> dividerPositions = new ArrayList<>();

    @XmlElements(
            {
                    @XmlElement(name = "branch", type = DockContainerBranchDto.class),
                    @XmlElement(name = "leaf", type = DockContainerLeafDto.class)
            }
    )
    public List<DockContainerDto> children = new ArrayList<>();
}
