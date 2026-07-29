package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.persistence.api.storage.LayoutStorage;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for getting
 * {@link LayoutStorage} implementations.
 *
 * <p>The provider is the {@code ServiceLoader}-discoverable type. The
 * {@link LayoutStorage} returned by this provider does not need to be directly
 * discoverable.
 *
 * @author Phil Bryant
 */
public interface LayoutStorageProvider extends LayoutPersistenceComponentProvider {

    /**
     * Returns a {@link LayoutStorage} that can be used to persist a Bento
     * layout.
     * @return a {@link LayoutStorage} that can be used to persist a Bento
     * layout.
     */
    LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    );
}
