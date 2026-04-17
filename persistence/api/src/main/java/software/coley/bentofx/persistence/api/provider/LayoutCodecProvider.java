package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutCodec} implementations.
 *
 * @author Phil Bryant
 */
public interface LayoutCodecProvider {

    /**
     * Creates a {@link LayoutCodec}.
     * @return a {@link LayoutCodec}
     */
    LayoutCodec createLayoutCodec();
}
