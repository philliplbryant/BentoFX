package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import javafx.stage.Modality;
import org.jspecify.annotations.Nullable;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DragDropStage}.
 *
 * @author Phil Bryant
 */
public class DragDropStageDto {

    public @Nullable String title;

    public @Nullable Double x;

    public @Nullable Double y;

    public @Nullable Double width;

    public @Nullable Double height;

    public @Nullable Modality modality;

    public @Nullable Double opacity;

    public @Nullable Boolean iconified;

    public @Nullable Boolean fullScreen;

    public @Nullable Boolean maximized;

    public @Nullable Boolean alwaysOnTop;

    public @Nullable Boolean resizable;

    public @Nullable Boolean showing;

    public @Nullable Boolean focused;

    public @Nullable Boolean autoCloseWhenEmpty;

    public @Nullable DockContainerRootBranchDto dockContainerRootBranchDto;
}
