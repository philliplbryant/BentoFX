package software.coley.bentofx.persistence.api.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static software.coley.bentofx.persistence.api.storage.LayoutIdentifiers.MAX_JOINED_LENGTH;

class LayoutIdentifiersTest {

    private static final String LAYOUT_IDENTIFIER = "main-layout";
    private static final String CODEC_IDENTIFIER = "json";

    @Test
    void acceptsAnOrdinaryPair() {
        assertThatCode(() ->
                LayoutIdentifiers.requireValid(LAYOUT_IDENTIFIER, CODEC_IDENTIFIER)
        )
                .describedAs("an ordinary pair")
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsACodecIdentifierWrittenAsAnExtension() {
        assertThatCode(() ->
                LayoutIdentifiers.requireValid(LAYOUT_IDENTIFIER, ".json")
        )
                .describedAs("a codec identifier with a leading dot")
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsANameBeginningWithAPeriod() {
        assertThatCode(() ->
                LayoutIdentifiers.requireValid(".hidden-layout", CODEC_IDENTIFIER)
        )
                .describedAs("a layout identifier beginning with a period")
                .doesNotThrowAnyException();
    }

    @Test
    void requiresALayoutIdentifier() {
        assertThatNullPointerException()
                .describedAs("null layout identifier")
                .isThrownBy(() -> LayoutIdentifiers.requireValid(null, CODEC_IDENTIFIER))
                .withMessage("layoutIdentifier");
    }

    @Test
    void requiresACodecIdentifier() {
        assertThatNullPointerException()
                .describedAs("null codec identifier")
                .isThrownBy(() -> LayoutIdentifiers.requireValid(LAYOUT_IDENTIFIER, null))
                .withMessage("codecIdentifier");
    }

    @Test
    void rejectsABlankLayoutIdentifier() {
        assertThatIllegalArgumentException()
                .describedAs("blank layout identifier")
                .isThrownBy(() -> LayoutIdentifiers.requireValid("  ", CODEC_IDENTIFIER))
                .withMessageContaining("layoutIdentifier")
                .withMessageContaining("blank");
    }

    @Test
    void rejectsAPathSeparatorInEitherIdentifier() {
        assertThatIllegalArgumentException()
                .describedAs("layout identifier containing a forward slash")
                .isThrownBy(() -> LayoutIdentifiers.requireValid("nested/layout", CODEC_IDENTIFIER))
                .withMessageContaining("layoutIdentifier");

        assertThatIllegalArgumentException()
                .describedAs("codec identifier containing a backslash")
                .isThrownBy(() -> LayoutIdentifiers.requireValid(LAYOUT_IDENTIFIER, "json\\x"))
                .withMessageContaining("codecIdentifier");
    }

    @Test
    void rejectsAnIdentifierThatNamesADirectory() {
        assertThatIllegalArgumentException()
                .describedAs("layout identifier that is a parent directory")
                .isThrownBy(() -> LayoutIdentifiers.requireValid("..", CODEC_IDENTIFIER))
                .withMessageContaining("layoutIdentifier");
    }

    @Test
    void rejectsALayoutIdentifierReservedForADevice() {
        assertThatIllegalArgumentException()
                .describedAs("layout identifier naming a device")
                .isThrownBy(() -> LayoutIdentifiers.requireValid("nul", CODEC_IDENTIFIER))
                .withMessageContaining("device");

        assertThatIllegalArgumentException()
                .describedAs("device name ahead of a suffix")
                .isThrownBy(() -> LayoutIdentifiers.requireValid("COM1.old", CODEC_IDENTIFIER))
                .withMessageContaining("device");
    }

    @Test
    void rejectsALayoutIdentifierReservedForASuperscriptDevice() {
        // Built from a code point rather than written as a literal so that the
        // test does not depend on the encoding this source is read with, which is
        // also why the names it checks are escapes.
        final String superscriptDevice = "COM" + (char) 0x00B9;

        assertThatIllegalArgumentException()
                .describedAs("layout identifier naming a superscript device")
                .isThrownBy(() ->
                        LayoutIdentifiers.requireValid(superscriptDevice, CODEC_IDENTIFIER)
                )
                .withMessageContaining("device");
    }

    @Test
    void rejectsEveryCharacterNoFileNameMayHold() {
        for (final char forbidden : "<>:\"|?*".toCharArray()) {
            assertThatIllegalArgumentException()
                    .describedAs("layout identifier containing " + forbidden)
                    .isThrownBy(() ->
                            LayoutIdentifiers.requireValid(
                                    "main" + forbidden + "layout",
                                    CODEC_IDENTIFIER
                            )
                    )
                    .withMessageContaining("layoutIdentifier");
        }
    }

    @Test
    void rejectsAControlCharacter() {
        assertThatIllegalArgumentException()
                .describedAs("codec identifier containing a zero byte")
                .isThrownBy(() ->
                        LayoutIdentifiers.requireValid(
                                LAYOUT_IDENTIFIER,
                                "js" + (char) 0 + "on"
                        )
                )
                .withMessageContaining("code 0");

        assertThatIllegalArgumentException()
                .describedAs("layout identifier containing a tab")
                .isThrownBy(() ->
                        LayoutIdentifiers.requireValid("main\tlayout", CODEC_IDENTIFIER)
                )
                .withMessageContaining("code 9");
    }

    @Test
    void rejectsANameEndingWithASpaceOrPeriod() {
        assertThatIllegalArgumentException()
                .describedAs("layout identifier ending with a space")
                .isThrownBy(() ->
                        LayoutIdentifiers.requireValid("main-layout ", CODEC_IDENTIFIER)
                )
                .withMessageContaining("space or a period");

        assertThatIllegalArgumentException()
                .describedAs("codec identifier ending with a period")
                .isThrownBy(() ->
                        LayoutIdentifiers.requireValid(LAYOUT_IDENTIFIER, "json.")
                )
                .withMessageContaining("space or a period");
    }

    @Test
    void rejectsAPairThatIsTooLongTogetherEvenWhenEachHalfFits() {
        // Each half is inside the limit; joined with the separator they are one
        // character over it, which is the case a per-identifier check misses.
        final String longLayoutIdentifier =
                "a".repeat(MAX_JOINED_LENGTH - CODEC_IDENTIFIER.length());

        assertThatCode(() ->
                LayoutIdentifiers.requireValid(
                        longLayoutIdentifier.substring(1),
                        CODEC_IDENTIFIER
                )
        )
                .describedAs("a pair that exactly fits")
                .doesNotThrowAnyException();

        assertThatIllegalArgumentException()
                .describedAs("a pair one character too long together")
                .isThrownBy(() ->
                        LayoutIdentifiers.requireValid(longLayoutIdentifier, CODEC_IDENTIFIER)
                )
                .withMessageContaining(String.valueOf(MAX_JOINED_LENGTH));
    }
}
