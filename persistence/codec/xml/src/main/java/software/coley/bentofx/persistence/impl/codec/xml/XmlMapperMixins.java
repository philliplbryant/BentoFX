package software.coley.bentofx.persistence.impl.codec.xml;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.*;

import static java.util.Objects.requireNonNull;

/**
 * Utility for registering all DTO Jackson XML mix-ins on an ObjectMapper.
 */
final class XmlMapperMixins {

    private XmlMapperMixins() {
        throw new IllegalStateException("Utility class");
    }

    static ObjectMapper registerAll(final ObjectMapper objectMapper) {
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
        objectMapper.addMixIn(LayoutMetadataDto.class, LayoutMetadataDtoXmlMixin.class);

        return objectMapper;
    }
}
