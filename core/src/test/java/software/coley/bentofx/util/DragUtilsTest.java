package software.coley.bentofx.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for {@link DragUtils}'s drag-n-drop payload parsing.
 *
 * @author Phil Bryant
 */
class DragUtilsTest {

    private static final String PREFIX_FIELD =
            DragUtils.PREFIX.substring(0, DragUtils.PREFIX.length() - 1);
    private static final String DRAG_GROUP_MASK = "5";
    private static final String DOCKABLE_IDENTIFIER = "dockable-1";
    private static final String EMPTY_IDENTIFIER = "";
    private static final String DROP_TARGET_NAME = DragDropTarget.HEADER.name();

    /**
     * {@code String.split(String)} uses a limit of {@code 0}, which drops
     * trailing empty fields. A dockable may legitimately have an empty-string
     * identifier, and when no drop target is appended that identifier is the
     * trailing field - so the naive split silently loses it, turning a real
     * (empty) identifier into a missing one.
     */
    @Test
    void splitContentPreservesATrailingEmptyIdentifier() {
        String raw =
                DragUtils.PREFIX + DRAG_GROUP_MASK + ";" + EMPTY_IDENTIFIER;

        String[] parts = DragUtils.splitContent(raw);

        assertThat(parts)
                .describedAs("fields parsed from [" + raw + "]")
                .containsExactly(
                        PREFIX_FIELD,
                        DRAG_GROUP_MASK,
                        EMPTY_IDENTIFIER
                );
    }

    /**
     * The ordinary case - a non-empty identifier and no drop target - must
     * keep working the same as before.
     */
    @Test
    void splitContentParsesANonEmptyIdentifierWithNoDropTarget() {
        String raw =
                DragUtils.PREFIX + DRAG_GROUP_MASK + ";" + DOCKABLE_IDENTIFIER;

        String[] parts = DragUtils.splitContent(raw);

        assertThat(parts)
                .describedAs("fields parsed from [" + raw + "]")
                .containsExactly(
                        PREFIX_FIELD,
                        DRAG_GROUP_MASK,
                        DOCKABLE_IDENTIFIER
                );
    }

    /**
     * A completed payload appends a fourth, drop-target field after the
     * identifier - this must still parse the same as before the fix.
     */
    @Test
    void splitContentParsesAllFourFieldsWhenADropTargetIsAppended() {
        String raw =
                DragUtils.PREFIX + DRAG_GROUP_MASK + ";" + DOCKABLE_IDENTIFIER +
                        ";" + DROP_TARGET_NAME;

        String[] parts = DragUtils.splitContent(raw);

        assertThat(parts)
                .describedAs("fields parsed from [" + raw + "]")
                .containsExactly(
                        PREFIX_FIELD,
                        DRAG_GROUP_MASK,
                        DOCKABLE_IDENTIFIER,
                        DROP_TARGET_NAME
                );
    }
}
