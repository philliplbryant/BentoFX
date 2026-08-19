package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import org.jspecify.annotations.Nullable;

/**
 * Metadata about a persisted BentoFX docking layout.
 *
 * @author Phil Bryant
 */
public class LayoutMetadataDto {

    /**
     * Version of the persisted layout schema.
     */
    public @Nullable Integer schemaVersion;

    /**
     * The layout's human-readable name, or {@code null} when it was saved
     * without one.
     */
    public @Nullable String displayName;
}
