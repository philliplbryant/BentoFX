package software.coley.bentofx.persistence.impl.codec.xml.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.xml.XmlLayoutCodec;

import static org.assertj.core.api.Assertions.assertThat;

class XmlLayoutCodecProviderTest {

    @Test
    void createLayoutCodecReturnsXmlLayoutCodec() {
        final XmlLayoutCodecProvider provider = new XmlLayoutCodecProvider();

        final LayoutCodec codec = provider.createLayoutCodec();

        assertThat(codec)
                .isNotNull()
                .isInstanceOf(XmlLayoutCodec.class);
    }
}
