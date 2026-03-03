/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.xml.bind.annotation.*;
import javafx.geometry.Orientation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainer}.
 *
 * @author Phil Bryant
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(BRANCH_ELEMENT_NAME)
public class DockContainerBranchDto extends DockContainerDto {

    @XmlElementWrapper(name = DIVIDER_POSITION_LIST_ELEMENT_NAME)
    @XmlElement(name = DIVIDER_ELEMENT_NAME)
    @JsonProperty(DIVIDER_ELEMENT_NAME)
    public final @NotNull List<@NotNull DividerPositionDto> dividerPositions =
            new ArrayList<>();
    @XmlElements({
            @XmlElement(name = BRANCH_ELEMENT_NAME, type = DockContainerBranchDto.class),
            @XmlElement(name = LEAF_ELEMENT_NAME, type = DockContainerLeafDto.class)
    })
    public final @NotNull List<@NotNull DockContainerDto> children =
            new ArrayList<>();
    @XmlAttribute
    public Orientation orientation; // "HORIZONTAL" or "VERTICAL"
}
