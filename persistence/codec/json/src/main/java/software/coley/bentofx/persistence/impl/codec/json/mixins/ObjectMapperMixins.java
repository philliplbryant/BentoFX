package software.coley.bentofx.persistence.impl.codec.json.mixins;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockableDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DragDropStageDto;

import static java.util.Objects.requireNonNull;

/**
 * Utility for registering all DTO Jackson JSON mix-ins on an ObjectMapper.
 *
 * @author Phil Bryant
 */
public final class ObjectMapperMixins {

    private ObjectMapperMixins() {
        throw new IllegalStateException("Utility class");
    }

    public static ObjectMapper registerAll(final ObjectMapper objectMapper) {
        requireNonNull(objectMapper);

        objectMapper.addMixIn(BentoStateDto.class, BentoStateDtoJsonMixin.class);
        objectMapper.addMixIn(DividerPositionDto.class, DividerPositionDtoJsonMixin.class);
        objectMapper.addMixIn(DockableDto.class, DockableDtoJsonMixin.class);
        objectMapper.addMixIn(DockContainerDto.class, DockContainerDtoJsonMixin.class);
        objectMapper.addMixIn(DockContainerBranchDto.class, DockContainerBranchDtoJsonMixin.class);
        objectMapper.addMixIn(DockContainerLeafDto.class, DockContainerLeafDtoJsonMixin.class);
        objectMapper.addMixIn(DockContainerRootBranchDto.class, DockContainerRootBranchDtoJsonMixin.class);
        objectMapper.addMixIn(DockingLayoutDto.class, DockingLayoutDtoJsonMixin.class);
        objectMapper.addMixIn(DragDropStageDto.class, DragDropStageDtoJsonMixin.class);

        return objectMapper;
    }
}
