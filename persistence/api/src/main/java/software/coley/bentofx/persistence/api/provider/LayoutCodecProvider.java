package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for getting
 * {@link LayoutCodec} implementations.
 *
 * <p>The provider is the {@code ServiceLoader}-discoverable type. The
 * {@link LayoutCodec} returned by this provider does not need to be directly
 * discoverable.
 *
 * @author Phil Bryant
 */
public interface LayoutCodecProvider extends LayoutPersistenceComponentProvider {

    /**
     * Returns a {@link LayoutCodec}.
     * @return a {@link LayoutCodec}
     */
    LayoutCodec getLayoutCodec();
}
