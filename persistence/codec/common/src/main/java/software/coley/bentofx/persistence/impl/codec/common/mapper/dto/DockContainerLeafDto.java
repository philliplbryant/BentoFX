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
import javafx.geometry.Side;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DOCKABLES_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.DOCKABLE_ELEMENT_NAME;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DockContainerLeafDto extends DockContainerDto {

    @XmlAttribute
    public String selectedDockableId;

    @XmlAttribute
    public Side side;

    @XmlElementWrapper(name = DOCKABLES_ELEMENT_NAME)
    @XmlElement(name = DOCKABLE_ELEMENT_NAME)
    public List<DockableDto> dockables = new ArrayList<>();
}
