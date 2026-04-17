package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import org.jspecify.annotations.Nullable;

/**
 * Mappable Data Transfer Object representing the layout state of a
 * {@code DockContainer}.
 *
 * @author Phil Bryant
 */
public abstract class DockContainerDto {

    public @Nullable String identifier;

    public @Nullable Boolean pruneWhenEmpty;
}
