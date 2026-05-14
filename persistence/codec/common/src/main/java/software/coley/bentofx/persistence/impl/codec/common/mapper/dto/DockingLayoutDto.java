package software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Mappable Data Transfer Object representing the state of a BentoFX layout.
 *
 * @author Phil Bryant
 */
public class DockingLayoutDto {

    public final List<BentoStateDto> bentoStates =
            new ArrayList<>();
}
