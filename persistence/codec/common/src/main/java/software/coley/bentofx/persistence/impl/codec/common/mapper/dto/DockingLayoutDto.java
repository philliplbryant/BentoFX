package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mappable Data Transfer Object representing the state of a BentoFX layout.
 *
 * @author Phil Bryant
 */
public class DockingLayoutDto {

    private static final int CURRENT_SCHEMA_VERSION = 1;

    public @Nullable LayoutMetadataDto metadata;

    public final List<BentoStateDto> bentoStates =
            new ArrayList<>();

    public static int getCurrentSchemaVersion() {
        return CURRENT_SCHEMA_VERSION;
    }
}
