package software.coley.bentofx.persistence.testfixtures.provider;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.testfixtures.codec.TestLayoutCodec;

/**
 * Configurable {@link LayoutCodecProvider} for provider-selection tests.
 */
public final class TestLayoutCodecProvider extends AbstractTestLayoutProvider implements LayoutCodecProvider {
    private int createdCodecCount;

    public TestLayoutCodecProvider(
            final String identifier,
            final boolean defaultProvider
    ) {
        super(identifier, defaultProvider);
    }

    @Override
    public LayoutCodec getLayoutCodec() {
        createdCodecCount++;
        return new TestLayoutCodec(getIdentifier());
    }

    public int getCreatedCodecCount() {
        return createdCodecCount;
    }
}
