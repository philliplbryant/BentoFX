package software.coley.bentofx.persistence.testfixtures.provider;

/**
 * Shared configurable provider metadata for persistence provider tests.
 */
public abstract class AbstractTestLayoutProvider {
    private final String identifier;
    private final boolean defaultProvider;

    protected AbstractTestLayoutProvider(
            final String identifier,
            final boolean defaultProvider
    ) {
        this.identifier = identifier;
        this.defaultProvider = defaultProvider;
    }

    public final String getIdentifier() {
        return identifier;
    }

    public final boolean isDefault() {
        return defaultProvider;
    }
}
