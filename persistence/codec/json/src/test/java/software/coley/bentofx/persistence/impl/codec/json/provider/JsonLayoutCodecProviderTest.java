package software.coley.bentofx.persistence.impl.codec.json.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.json.JsonLayoutCodec;

import static org.assertj.core.api.Assertions.assertThat;

class JsonLayoutCodecProviderTest {

    @Test
    void exposesJsonIdentifierAndIsNotDefault() {
        final JsonLayoutCodecProvider provider = new JsonLayoutCodecProvider();

        assertThat(provider.getIdentifier())
                .isEqualTo(JsonLayoutCodec.EXTENSION);
        assertThat(provider.isDefault())
                .isFalse();
    }

    @Test
    void getLayoutCodecReturnsJsonLayoutCodec() {
        final JsonLayoutCodecProvider provider = new JsonLayoutCodecProvider();

        final LayoutCodec codec = provider.getLayoutCodec();

        assertThat(codec)
                .isNotNull()
                .isInstanceOf(JsonLayoutCodec.class);
        assertThat(codec.getIdentifier())
                .isEqualTo(JsonLayoutCodec.EXTENSION);
    }
}
