package software.coley.bentofx.persistence.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class LayoutPersistenceProfileTest {

    @Test
    void factoryCreatesProfileWithOnlyLayoutIdentifier() {
        final LayoutPersistenceProfile profile = LayoutPersistenceProfile.of("main-layout");

        assertThat(profile.layoutIdentifier())
                .isEqualTo("main-layout");
        assertThat(profile.codecIdentifier())
                .isNull();
        assertThat(profile.storageIdentifier())
                .isNull();
    }

    @Test
    void profileRequiresLayoutIdentifier() {
        assertThatNullPointerException()
                .isThrownBy(() -> new LayoutPersistenceProfile(null, "json", "file"))
                .withMessage("layoutIdentifier");
    }
}
