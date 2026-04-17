package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import javafx.geometry.Side;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainerLeaf}.
 *
 * @author Phil Bryant
 */
public class DockContainerLeafDto extends DockContainerDto {

    public final List<DockableDto> dockables =
            new ArrayList<>();

    public @Nullable String selectedDockableIdentifier;

    public @Nullable Side side;

    public @Nullable Boolean isResizableWithParent;

    public @Nullable Boolean isCanSplit;

    public @Nullable Double uncollapsedSizePx;

    public @Nullable Boolean isCollapsed;
}
