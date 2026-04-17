package software.coley.bentofx.persistence.impl.codec.xml.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.xml.XmlLayoutCodec;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XmlLayoutCodecProviderTest {

    @Test
    void createLayoutCodecReturnsXmlLayoutCodec() {
        final XmlLayoutCodecProvider provider = new XmlLayoutCodecProvider();

        final LayoutCodec codec = provider.createLayoutCodec();

        assertNotNull(codec);
        assertInstanceOf(XmlLayoutCodec.class, codec);
    }
}
