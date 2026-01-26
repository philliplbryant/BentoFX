/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.xml.bind.annotation.*;
import javafx.geometry.Side;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainerLeaf}.
 *
 * @author Phil Bryant
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(LEAF_ELEMENT_NAME)
public class DockContainerLeafDto extends DockContainerDto {

    @XmlAttribute
    public String selectedDockableIdentifier;

    @XmlAttribute
    public Side side;

    @XmlAttribute
    public Boolean isResizableWithParent;

    @XmlAttribute
    public Boolean isCanSplit;

    @XmlAttribute
    public Double uncollapsedSizePx;

    @XmlElementWrapper(name = DOCKABLE_LIST_ELEMENT_NAME)
    @XmlElement(name = DOCKABLE_ELEMENT_NAME)
    @JsonProperty(DOCKABLE_ELEMENT_NAME)
    public List<DockableDto> dockables = new ArrayList<>();
}
