package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.persistence.api.storage.LayoutStorage;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for getting
 * {@link LayoutStorage} implementations.
 *
 * @author Phil Bryant
 */
public interface LayoutStorageProvider {

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
