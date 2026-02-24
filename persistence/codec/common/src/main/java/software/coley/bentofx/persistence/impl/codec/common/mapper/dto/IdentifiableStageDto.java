/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.*;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.ROOT_BRANCH_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.ROOT_BRANCH_LIST_ELEMENT_NAME;


/**
 * Mappable Data Transfer Object representing the layout state of an
 * {@code IdentifiableStage}.
 *
 * @author Phil Bryant
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentifiableStageDto {

    @XmlAttribute
    public String identifier;
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
    public Modality modality;
    @XmlAttribute
    public Double opacity;
    @XmlAttribute
    public Boolean iconified;
    @XmlAttribute
    public Boolean fullScreen;
    @XmlAttribute
    public Boolean maximized;
    @XmlAttribute
    public Boolean alwaysOnTop;
    @XmlAttribute
    public Boolean resizable;
    @XmlAttribute
    public Boolean showing;
    @XmlAttribute
    public Boolean focused;

    @XmlElementWrapper(name = ROOT_BRANCH_LIST_ELEMENT_NAME)
    @XmlElement(name = ROOT_BRANCH_ELEMENT_NAME)
    @JsonProperty(ROOT_BRANCH_ELEMENT_NAME)
    public List<DockContainerRootBranchDto> dockContainerRootBranches = new ArrayList<>();
}
