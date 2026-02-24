package software.coley.bentofx;

import jakarta.annotation.Nonnull;

/**
 * Outline of an <i>(ideally uniquely)</i> identifiable object.
 *
 * @author Matt Coley
 */
public interface Identifiable {
	/**
	 * @return This objects identifier.
	 */
	@Nonnull
	String getIdentifier();

	/**
	 * @param other
	 * 		Another identifiable object.
	 *
	 * @return {@code true} when the other object has the same identifier.
	 */
    default boolean matchesIdentity(@Nonnull Identifiable other) {
        return this.getIdentifier().equals(other.getIdentifier());
    }
}
