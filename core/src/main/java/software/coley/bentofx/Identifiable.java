package software.coley.bentofx;

/**
 * Outline of an <i>(ideally uniquely)</i> identifiable object.
 *
 * @author Matt Coley
 */
public interface Identifiable {

	/**
	 * {@return This object's identifier.}
	 */
	String getIdentifier();

	/**
     * {@return {@code true} when the other object has the same identifier.}
     * @param other another identifiable object.
	 */
	default boolean matchesIdentity(final Identifiable other) {
		return this.getIdentifier().equals(other.getIdentifier());
	}
}
