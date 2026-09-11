package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import org.jspecify.annotations.Nullable;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code Dockable}.
 *
 * @author Phil Bryant
 */
public class DockableDto {

    public @Nullable String identifier;

    public @Nullable String title;

    public @Nullable String tooltipText;

    public @Nullable Integer dragGroupMask;

    public @Nullable Boolean isClosable;
}
