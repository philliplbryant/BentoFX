package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.ROOT_BRANCH_ELEMENT_NAME;

/**
 * Jackson XML mix-in for {@code DragDropStageDto}.
 *
 * @author Phil Bryant
 */
@JsonInclude(NON_NULL)
public abstract class DragDropStageDtoXmlMixin {

    @JacksonXmlProperty(isAttribute = true)
    public String title;

    @JacksonXmlProperty(isAttribute = true)
    public Double x;

    @JacksonXmlProperty(isAttribute = true)
    public Double y;

    @JacksonXmlProperty(isAttribute = true)
    public Double width;

    @JacksonXmlProperty(isAttribute = true)
    public Double height;

    @JacksonXmlProperty(isAttribute = true)
    public javafx.stage.Modality modality;

    @JacksonXmlProperty(isAttribute = true)
    public Double opacity;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean iconified;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean fullScreen;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean maximized;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean alwaysOnTop;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean resizable;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean showing;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean focused;

    @JacksonXmlProperty(isAttribute = true)
    public Boolean autoCloseWhenEmpty;

    @JacksonXmlProperty(localName = ROOT_BRANCH_ELEMENT_NAME)
    public DockContainerRootBranchDto dockContainerRootBranchDto;
}
