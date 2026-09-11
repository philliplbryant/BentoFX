package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mappable Data Transfer Object representing the state of a BentoFX layout.
 *
 * @author Phil Bryant
 */
public class BentoStateDto {

    public @Nullable String identifier;

    public final List<DockContainerRootBranchDto> rootBranches =
            new ArrayList<>();

    public final List<DragDropStageDto> dragDropStages =
            new ArrayList<>();
}
