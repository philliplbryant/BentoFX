package software.coley.bentofx.persistence.testfixtures.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.testfixtures.storage.TestLayoutStorage;

/**
 * Configurable {@link LayoutStorageProvider} for provider-selection tests.
 */
public final class TestLayoutStorageProvider implements LayoutStorageProvider {
    private final String identifier;
    private final boolean defaultProvider;
    private @Nullable String layoutIdentifier;
    private @Nullable String codecIdentifier;

    public TestLayoutStorageProvider(
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
    public LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        this.layoutIdentifier = layoutIdentifier;
        this.codecIdentifier = codecIdentifier;
        return new TestLayoutStorage();
    }

    public @Nullable String getLayoutIdentifier() {
        return layoutIdentifier;
    }

    public @Nullable String getCodecIdentifier() {
        return codecIdentifier;
    }
}
