/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import javafx.stage.Modality;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.ROOT_BRANCH_ELEMENT_NAME;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DragDropStage}.
 *
 * @author Phil Bryant
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DragDropStageDto {

    @XmlAttribute
    public Boolean autoCloseWhenEmpty;
    @XmlAttribute
    public String title;
    @XmlAttribute
    public Double x;
    @XmlAttribute
    public Double y;
    @XmlAttribute
    public Double width;
    @XmlAttribute
    public Double height;

    @XmlAttribute
    public Boolean iconified;
    @XmlAttribute
    public Boolean fullScreen;
    @XmlAttribute
    public Boolean maximized;
    @XmlAttribute
    public Modality modality;

    @XmlElement(name = ROOT_BRANCH_ELEMENT_NAME)
    @JsonProperty(ROOT_BRANCH_ELEMENT_NAME)
    public DockContainerRootBranchDto dockContainerRootBranchDto;
}
