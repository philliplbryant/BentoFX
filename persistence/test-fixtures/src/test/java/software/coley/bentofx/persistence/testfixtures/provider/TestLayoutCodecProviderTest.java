package software.coley.bentofx.persistence.testfixtures.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestLayoutCodecProviderTest {

    @Test
    void exposesIdentifierAndDefaultFlag() {
        final TestLayoutCodecProvider provider = new TestLayoutCodecProvider("json", true);

        assertThat(provider.getIdentifier())
                .isEqualTo("json");
        assertThat(provider.isDefault())
                .isTrue();
    }

    @Test
    void createsCodecWithMatchingIdentifierAndCountsCreations() {
        final TestLayoutCodecProvider provider = new TestLayoutCodecProvider("json", false);

        assertThat(provider.getLayoutCodec().getIdentifier())
                .isEqualTo("json");
        assertThat(provider.getCreatedCodecCount())
                .isEqualTo(1);
    }
}
