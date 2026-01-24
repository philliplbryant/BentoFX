/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.BRANCH_ELEMENT_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.LEAF_ELEMENT_NAME;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainer}.
 *
 * @author Phil Bryant
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // Defines the JSON property for type distinction
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DockContainerBranchDto.class, name = BRANCH_ELEMENT_NAME),
        @JsonSubTypes.Type(value = DockContainerLeafDto.class, name = LEAF_ELEMENT_NAME)
})
public abstract class DockContainerDto {

    @XmlAttribute
    public String identifier;
}
