package software.coley.boxfx.demo.persistence.provider;

import org.jspecify.annotations.Nullable;

/**
 * Data object containing properties for a {@code Dockable}.
 * @param identifier the {@code Dockable} identifier.
 * @param tooltip the tooltip text to display for the {@code Dockable}.
 *
 * @author Phil Bryant
 */
public record DockableProperties(String identifier, @Nullable String tooltip) {

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof final DockableProperties thatLabel) {
            return this.identifier.equals(thatLabel.identifier);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
