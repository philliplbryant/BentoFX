package software.coley.bentofx.persistence.impl.codec.json.provider;

import software.coley.bentofx.persistence.core.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.core.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.json.JsonLayoutCodec;

/**
 * JSON (JavaScript Object Notation) implementation of the
 * {@link LayoutCodecProvider} Service Provider Interface.
 *
 * @author Phil Bryant
 */
public class JsonLayoutCodecProvider implements LayoutCodecProvider {

    @Override
    public String getIdentifier() {
        return JsonLayoutCodec.CODEC_IDENTIFIER;
    }

    @Override
    public LayoutCodec getLayoutCodec() {
        return new JsonLayoutCodec();
    }
}
