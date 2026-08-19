package software.coley.bentofx.persistence.core.api.state;

import java.util.Objects;

/**
 * Represents the layout state of an {@code Identifiable}.
 *
 * @author Phil Bryant
 */
public abstract class IdentifiableState {

    private final String identifier;

    /**
     * Constructor.
     * @param identifier the identifier distinguishing this state from other
     * states of the same type.
     */
    protected IdentifiableState(
            final String identifier
    ) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    /**
     * {@return the identifier distinguishing this state from other states of
     * the same type.}
     */
    public String getIdentifier() {
        return identifier;
    }
}
