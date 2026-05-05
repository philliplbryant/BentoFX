package software.coley.bentofx.persistence.api.state;

import java.util.Objects;

/**
 * Represents the layout state of an {@code Identifiable}.
 *
 * @author Phil Bryant
 */
public abstract class IdentifiableState {

    private final String identifier;

    protected IdentifiableState(
            final String identifier
    ) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    public String getIdentifier() {
        return identifier;
    }
}
