package software.coley.bentofx.persistence.impl.codec.xml.mixins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
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
 * Utility for registering all DTO Jackson XML mix-ins on an ObjectMapper.
 */
public final class XmlMapperMixins {

    private XmlMapperMixins() {
        throw new IllegalStateException("Utility class");
    }

    public static @NonNull ObjectMapper registerAll(final @NonNull ObjectMapper objectMapper) {
        requireNonNull(objectMapper);

        objectMapper.addMixIn(DockingLayoutDto.class, DockingLayoutDtoXmlMixin.class);
        objectMapper.addMixIn(BentoStateDto.class, BentoStateDtoXmlMixin.class);
        objectMapper.addMixIn(DividerPositionDto.class, DividerPositionDtoXmlMixin.class);
        objectMapper.addMixIn(DockableDto.class, DockableDtoXmlMixin.class);
        objectMapper.addMixIn(DockContainerDto.class, DockContainerDtoXmlMixin.class);
        objectMapper.addMixIn(DockContainerBranchDto.class, DockContainerBranchDtoXmlMixin.class);
        objectMapper.addMixIn(DockContainerLeafDto.class, DockContainerLeafDtoXmlMixin.class);
        objectMapper.addMixIn(DockContainerRootBranchDto.class, DockContainerRootBranchDtoXmlMixin.class);
        objectMapper.addMixIn(DragDropStageDto.class, DragDropStageDtoXmlMixin.class);

        return objectMapper;
    }
}
