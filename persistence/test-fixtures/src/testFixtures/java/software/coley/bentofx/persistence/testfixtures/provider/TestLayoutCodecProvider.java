package software.coley.bentofx.persistence.testfixtures.provider;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.testfixtures.codec.TestLayoutCodec;

/**
 * Configurable {@link LayoutCodecProvider} for provider-selection tests.
 */
public final class TestLayoutCodecProvider implements LayoutCodecProvider {
    private final String identifier;
    private final boolean defaultProvider;
    private int createdCodecCount;

    public TestLayoutCodecProvider(
            final String identifier,
            final boolean defaultProvider
    ) {
        this.identifier = identifier;
        this.defaultProvider = defaultProvider;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isDefault() {
        return defaultProvider;
    }

    @Override
    public LayoutCodec getLayoutCodec() {
        createdCodecCount++;
        return new TestLayoutCodec(identifier);
    }

    public int getCreatedCodecCount() {
        return createdCodecCount;
    }
}
