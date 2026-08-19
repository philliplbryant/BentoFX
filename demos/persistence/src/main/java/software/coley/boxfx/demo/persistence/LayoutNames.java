package software.coley.boxfx.demo.persistence;

import software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers;

import java.util.Locale;

/**
 * Turns the name a user types for a docking layout into the identifier the
 * layout is stored under.
 *
 * <p>Its own class rather than a static method on {@link LayoutsMenu}: loading
 * a JavaFX control starts the toolkit, so a derivation living on the menu could
 * not be reached from a test that does not raise a window.</p>
 *
 * @author Phil Bryant
 */
public final class LayoutNames {

	/**
	 * The most characters a derived layout identifier may run to.
	 *
	 * <p>Well short of what storage allows, so that a long name is shortened
	 * here rather than refused there.</p>
	 */
	private static final int MAX_LAYOUT_IDENTIFIER_LENGTH = 64;

	private LayoutNames() {
		// Static members only.
	}

	/**
	 * {@return the derived layout identifier for a name a user typed, which is
	 * empty when the name held nothing an identifier can be made of.}
	 *
	 * <p>"Multi-Monitor" becomes "multi-monitor". The framework stores the
	 * typed name as a display name but addresses the layout by this identifier,
	 * and derives nothing itself.</p>
	 *
	 * <p>Only letters and digits survive, so the result cannot be a path, a
	 * directory, a name holding a character no file name may hold, or a name
	 * ending in a space or a period. What it can still be is nothing at all,
	 * the identifier this framework reserves, or a name Windows resolves to a
	 * device, which is why the result is still put to
	 * {@link LayoutIdentifiers#findUserLayoutProblem(String)}.</p>
	 *
	 * @param displayName the name the user typed.
	 */
	public static String toIdentifier(final String displayName) {

		// Cut before normalizing rather than after. Normalizing only ever
		// shortens, so cutting first bounds the identifier without leaving the
		// dash that a cut through a run of punctuation would.
		final String cutName =
				displayName.length() <= MAX_LAYOUT_IDENTIFIER_LENGTH ?
						displayName :
						displayName.substring(0, MAX_LAYOUT_IDENTIFIER_LENGTH);

		return cutName.toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "-")
				.replaceAll("^-+|-+$", "");
	}
}
