package software.coley.bentofx.persistence.core.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class LayoutPersistenceProfileTest {

    private static final String LAYOUT_IDENTIFIER = "main-layout";

    @Test
    void factoryCreatesProfileWithOnlyLayoutIdentifier() {
        final LayoutPersistenceProfile profile = LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER);

        assertThat(profile.layoutIdentifier())
                .describedAs("profile.layoutIdentifier()")
                .isEqualTo(LAYOUT_IDENTIFIER);
        assertThat(profile.codecIdentifier())
                .describedAs("profile.codecIdentifier()")
                .isNull();
        assertThat(profile.storageIdentifier())
                .describedAs("profile.storageIdentifier()")
                .isNull();
    }

    @Test
    void profileRequiresLayoutIdentifier() {
        assertThatNullPointerException()
                .describedAs("null pointer validation")
                .isThrownBy(() -> new LayoutPersistenceProfile(null, "json", "file"))
                .withMessage("layoutIdentifier");
    }
}
