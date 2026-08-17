package software.coley.bentofx.persistence.api.storage;

import java.util.Locale;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * The rule for the two identifiers that name a stored layout, in one place
 * because a pair has to satisfy every {@link LayoutStorage} implementation at
 * once.
 *
 * <p>A layout is addressed by a layout identifier and a codec identifier.
 * Database-backed storage keeps them in two columns; file-backed storage joins
 * them into one file name. A pair that suits one and not the other is not a
 * usable pair, so the rule here is the intersection: what a column will hold,
 * and what a filesystem will accept as a single path component.</p>
 *
 * <p>Providers apply this where the application's strings arrive, so that a
 * layout which cannot be stored is refused at the call that named it rather than
 * at the save that needed it.</p>
 *
 * <p>The naming rules below are Windows' by origin, and deliberately applied
 * everywhere. A POSIX filesystem reserves no names at all and forbids only
 * {@code /} and a zero byte, so a rule wide enough for Windows is wide enough for
 * Linux and macOS - while a rule that only held on the platform it was written on
 * would let an application name a layout it cannot restore on the next machine
 * it ships to.</p>
 *
 * @author Phil Bryant
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file">
 * Naming Files, Paths, and Namespaces</a>, which is where the reserved names and
 * characters come from.
 */
public final class LayoutIdentifiers {

    /**
     * The most characters the two identifiers may take together, counting the
     * separator between them.
     *
     * <p>File-backed storage joins the pair into one path component, and every
     * mainstream filesystem stops at 255 characters per component. That is the
     * tightest ceiling any implementation imposes, so it is the one they all
     * publish - a database column wide enough for this is wide enough.</p>
     */
    public static final int MAX_JOINED_LENGTH = 255;

    /**
     * Names that Windows resolves to a device rather than to a file, whichever
     * directory they are used in. Matched against the identifier and against what
     * precedes its first {@code .}, because {@code NUL.txt} and {@code NUL.tar.gz}
     * are both the device.
     *
     * <p>The superscript forms are reserved as well: Windows reads the ISO/IEC
     * 8859-1 superscript digits as digits, so {@code COM} followed by a superscript
     * one is a device too. They are written as escapes to keep this source ASCII,
     * because a literal mangled by a source encoding would silently stop
     * matching.</p>
     */
    private static final Set<String> RESERVED_DEVICE_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
            "COM\u00B9", "COM\u00B2", "COM\u00B3",
            "LPT\u00B9", "LPT\u00B2", "LPT\u00B3"
    );

    /**
     * Characters no file name may contain. {@code /} and {@code \} are handled
     * separately, so that a name containing one is reported as a path rather than
     * as a bad character.
     */
    private static final String FORBIDDEN_CHARACTERS = "<>:\"|?*";

    private static final Set<String> DIRECTORY_NAMES = Set.of(".", "..");

    private LayoutIdentifiers() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Accepts a pair of identifiers that every storage implementation can use,
     * and rejects the rest.
     *
     * <p>The codec identifier may carry a leading {@code .}, since that is how
     * an extension is often written and file-backed storage strips it.</p>
     *
     * @param layoutIdentifier identifies the layout. Additionally may not be a
     * name reserved for a device, because file-backed storage makes it the
     * beginning of a file name.
     * @param codecIdentifier identifies the codec whose output is stored.
     * @throws NullPointerException if either identifier is {@code null}, naming
     * the one that was missing.
     * @throws IllegalArgumentException if either identifier is blank, contains a
     * path separator, names a directory, holds a character no file name may hold
     * ({@code < > : " | ? *} or a control character), ends with a space or a
     * period, or if the two together exceed {@link #MAX_JOINED_LENGTH}.
     */
    public static void requireValid(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        requireUsableName(layoutIdentifier, "layoutIdentifier");
        requireUsableName(codecIdentifier, "codecIdentifier");
        requireNotADeviceName(layoutIdentifier);

        // The two arrive as one path component joined by a '.', so what has to
        // fit is both of them plus that separator.
        final int joinedLength =
                layoutIdentifier.length() + 1 + codecIdentifier.length();

        if (joinedLength > MAX_JOINED_LENGTH) {
            throw new IllegalArgumentException(
                    "layoutIdentifier and codecIdentifier must take at most "
                            + MAX_JOINED_LENGTH + " characters together, but '"
                            + layoutIdentifier + "' and '" + codecIdentifier
                            + "' take " + joinedLength + "."
            );
        }
    }

    /**
     * Rejects an identifier that is not usable as part of a name.
     *
     * @param identifier the identifier to check.
     * @param parameterName what to call it when rejecting it.
     */
    private static void requireUsableName(
            final String identifier,
            final String parameterName
    ) {
        requireNonNull(identifier, parameterName);

        if (identifier.isBlank()) {
            throw new IllegalArgumentException(
                    parameterName + " must not be blank."
            );
        }

        if (identifier.indexOf('/') >= 0 || identifier.indexOf('\\') >= 0) {
            throw new IllegalArgumentException(
                    parameterName + " must be one name rather than a path, but was '"
                            + identifier + "'."
            );
        }

        if (DIRECTORY_NAMES.contains(identifier)) {
            throw new IllegalArgumentException(
                    parameterName + " must name a layout rather than a directory, but was '"
                            + identifier + "'."
            );
        }

        requireNoForbiddenCharacter(identifier, parameterName);

        // Windows keeps neither: the shell and the interface drop a trailing space
        // or period, so the name asked for and the name stored are not the same
        // name. A leading period is fine, and is how a hidden file is written.
        final char lastCharacter = identifier.charAt(identifier.length() - 1);

        if (lastCharacter == ' ' || lastCharacter == '.') {
            throw new IllegalArgumentException(
                    parameterName + " must not end with a space or a period, but was '"
                            + identifier + "'."
            );
        }
    }

    /**
     * Rejects an identifier containing a character that no file name may hold.
     *
     * @param identifier the identifier to check.
     * @param parameterName what to call it when rejecting it.
     */
    private static void requireNoForbiddenCharacter(
            final String identifier,
            final String parameterName
    ) {
        for (int index = 0; index < identifier.length(); index++) {
            final char character = identifier.charAt(index);

            // Everything below a space is a control character, which Windows
            // rejects outright and a zero byte ends a POSIX name early.
            if (character >= ' ' && FORBIDDEN_CHARACTERS.indexOf(character) < 0) {
                continue;
            }

            final String description = character < ' ' ?
                    "the character with code " + (int) character :
                    "'" + character + "'";

            throw new IllegalArgumentException(
                    parameterName + " must not contain " + description
                            + ", but has one at index " + index + "."
            );
        }
    }

    /**
     * Rejects an identifier that a filesystem would resolve to a device.
     *
     * @param layoutIdentifier the identifier to check.
     */
    private static void requireNotADeviceName(final String layoutIdentifier) {
        final int firstDot = layoutIdentifier.indexOf('.');

        final String baseName = firstDot < 0 ?
                layoutIdentifier :
                layoutIdentifier.substring(0, firstDot);

        if (RESERVED_DEVICE_NAMES.contains(baseName.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "layoutIdentifier must not be a name reserved for a device, but was '"
                            + layoutIdentifier + "'."
            );
        }
    }
}
