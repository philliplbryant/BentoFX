package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.persistence.api.storage.LayoutStorage;

/**
 * Supplies the {@link LayoutStorage} that decides where a layout is written to.
 *
 * <p>Discovered at runtime, so an application changes where layouts are kept by
 * changing which storage implementation it depends on - see
 * {@link LayoutPersistenceComponentProvider}. The provider is the discoverable
 * type; the {@link LayoutStorage} it returns does not need to be discoverable
 * itself.</p>
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
