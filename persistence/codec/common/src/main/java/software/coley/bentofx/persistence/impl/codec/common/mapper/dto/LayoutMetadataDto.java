package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

/**
 * Metadata about a persisted BentoFX docking layout payload.
 *
 * @author Phil Bryant
 */
public class LayoutMetadataDto {

    /**
     * Version of the persisted layout schema.
     */
    public Integer schemaVersion;

    /**
     * Identifier of the codec that encoded the layout payload.
     */
    public String codecIdentifier;
}
