package software.coley.bentofx.persistence.testfixtures.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.testfixtures.storage.TestLayoutStorage;

import static org.assertj.core.api.Assertions.assertThat;

class TestLayoutStorageProviderTest {

    @Test
    void exposesIdentifierAndDefaultFlag() {
        final TestLayoutStorageProvider provider = new TestLayoutStorageProvider("file", true);

        assertThat(provider.getIdentifier())
                .isEqualTo("file");
        assertThat(provider.isDefault())
                .isTrue();
    }

    @Test
    void createsStorageAndRecordsIdentifiers() {
        final TestLayoutStorageProvider provider = new TestLayoutStorageProvider("file", false);

        assertThat(provider.getLayoutStorage("layout", "json"))
                .isInstanceOf(TestLayoutStorage.class);
        assertThat(provider.getLayoutIdentifier())
                .isEqualTo("layout");
        assertThat(provider.getCodecIdentifier())
                .isEqualTo("json");
    }
}
