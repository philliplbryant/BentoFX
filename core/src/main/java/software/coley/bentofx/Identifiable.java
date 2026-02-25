package software.coley.bentofx;

import org.jetbrains.annotations.NotNull;

/**
 * Outline of an <i>(ideally uniquely)</i> identifiable object.
 *
 * @author Matt Coley
 */
public interface Identifiable {
	/**
	 * @return This objects identifier.
	 */
	@NotNull
	String getIdentifier();

	/**
	 * @param other
	 * 		Another identifiable object.
	 *
	 * @return {@code true} when the other object has the same identifier.
	 */
    default boolean matchesIdentity(@NotNull Identifiable other) {
        return this.getIdentifier().equals(other.getIdentifier());
    }
}
