package software.coley.bentofx.persistence.impl.codec.xml.provider;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.xml.XmlLayoutCodec;

/**
 * XML (eXtensible Markup Language) implementation of the
 * {@link LayoutCodecProvider} Service Provider Interface.
 *
 * @author Phil Bryant
 */
public class XmlLayoutCodecProvider implements LayoutCodecProvider {

    @Override
    public LayoutCodec getLayoutCodec() {
        return new XmlLayoutCodec();
    }
}
