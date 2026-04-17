package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import javafx.geometry.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainer}.
 *
 * @author Phil Bryant
 */
public class DockContainerBranchDto extends DockContainerDto {

    public final List<DividerPositionDto> dividerPositions =
            new ArrayList<>();

    public final List<DockContainerDto> children =
            new ArrayList<>();

    public @Nullable Orientation orientation; // "HORIZONTAL" or "VERTICAL"
}
