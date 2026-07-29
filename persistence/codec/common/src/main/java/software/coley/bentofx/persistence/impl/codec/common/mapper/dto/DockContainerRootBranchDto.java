package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import javafx.geometry.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainerRootBranch}.
 *
 * @author Phil Bryant
 */
public class DockContainerRootBranchDto {

    public @Nullable String identifier;

    public @Nullable Boolean pruneWhenEmpty;

    public @Nullable Orientation orientation;

    public final List<DividerPositionDto> dividerPositions =
            new ArrayList<>();

    public final List<DockContainerBranchDto> branches =
            new ArrayList<>();

    public @Nullable DockContainerLeafDto leaf;
}
