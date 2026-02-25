package software.coley.bentofx;

import org.jetbrains.annotations.NotNull;

/**
 * Outline of an object with access to its originating {@link Bento} instance.
 *
 * @author Matt Coley
 */
public interface BentoBacked {
	/**
	 * @return Bento instance responsible for this object.
	 */
	@NotNull
	Bento getBento();
}
