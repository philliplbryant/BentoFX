package software.coley.bentofx.persistence.impl.codec.xml.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.xml.XmlLayoutCodec;

import static org.assertj.core.api.Assertions.assertThat;

class XmlLayoutCodecProviderTest {

    @Test
    void exposesXmlIdentifierAndIsNotDefault() {
        final XmlLayoutCodecProvider provider = new XmlLayoutCodecProvider();

        assertThat(provider.getIdentifier())
                .isEqualTo(XmlLayoutCodec.EXTENSION);
        assertThat(provider.isDefault())
                .isFalse();
    }

    @Test
    void getLayoutCodecReturnsXmlLayoutCodec() {
        final XmlLayoutCodecProvider provider = new XmlLayoutCodecProvider();

        final LayoutCodec codec = provider.getLayoutCodec();

        assertThat(codec)
                .isNotNull()
                .isInstanceOf(XmlLayoutCodec.class);
        assertThat(codec.getIdentifier())
                .isEqualTo(XmlLayoutCodec.EXTENSION);
    }
}
