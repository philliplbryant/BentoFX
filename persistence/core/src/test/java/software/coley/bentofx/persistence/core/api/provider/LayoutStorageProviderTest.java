package software.coley.bentofx.persistence.core.api.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.core.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.testfixtures.storage.InMemoryLayoutStorage;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutStorageProviderTest {

    private static final String LAYOUT_IDENTIFIER = "layout-1";
    private static final String CODEC_IDENTIFIER = "json";

    private final LayoutStorageProvider provider = new LayoutStorageProvider() {
        @Override
        public String getIdentifier() {
            return "minimal";
        }

        @Override
        public LayoutStorage getLayoutStorage(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            return new InMemoryLayoutStorage();
        }
    };

    @Test
    void getLayoutIdentifiersDefaultsToAnEmptyList() {
        assertThat(provider.getLayoutIdentifiers(CODEC_IDENTIFIER))
                .describedAs("getLayoutIdentifiers() with no override")
                .isEmpty();
    }

    @Test
    void deleteLayoutDefaultsToFalse() {
        assertThat(provider.deleteLayout(LAYOUT_IDENTIFIER, CODEC_IDENTIFIER))
                .describedAs("deleteLayout() with no override")
                .isFalse();
    }
}
