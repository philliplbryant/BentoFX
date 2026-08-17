package software.coley.bentofx.persistence.testfixtures.provider;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.testfixtures.storage.TestLayoutStorage;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Configurable {@link LayoutStorageProvider} for provider-selection tests.
 *
 * <p>The recorded identifiers are held atomically because the persistence API
 * decides which thread calls a provider, so the test asserting on them is not the
 * thread that recorded them.</p>
 *
 * @author Phil Bryant
 */
public final class TestLayoutStorageProvider
        extends AbstractTestLayoutProvider
        implements LayoutStorageProvider {

    private final AtomicReference<@Nullable String> layoutIdentifier =
            new AtomicReference<>();
    private final AtomicReference<@Nullable String> codecIdentifier =
            new AtomicReference<>();

    /**
     * Creates a provider that reports the supplied identifier and default flag.
     *
     * @param identifier the identifier this provider answers to.
     * @param defaultProvider whether this provider is the default one.
     */
    public TestLayoutStorageProvider(
            final String identifier,
            final boolean defaultProvider
    ) {
        super(identifier, defaultProvider);
    }

    @Override
    public LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        this.layoutIdentifier.set(layoutIdentifier);
        this.codecIdentifier.set(codecIdentifier);
        return new TestLayoutStorage();
    }

    /**
     * {@return the layout identifier this provider was last asked for, or
     * {@code null} when it has not been asked.}
     */
    public @Nullable String getLayoutIdentifier() {
        return layoutIdentifier.get();
    }

    /**
     * {@return the codec identifier this provider was last asked for, or
     * {@code null} when it has not been asked.}
     */
    public @Nullable String getCodecIdentifier() {
        return codecIdentifier.get();
    }
}
