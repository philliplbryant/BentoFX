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
     * <p>
     * Each call must return a fresh instance, even for arguments it has already
     * been given. The caller takes ownership of what it is handed and closes it
     * (see {@link LayoutStorage#close()}), so an implementation that caches and
     * returns one instance per layout would have a closed saver take the
     * restorer's storage down with it.
     *
     * @return a {@link LayoutStorage} that can be used to persist a Bento
     * layout.
     */
    LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    );
}
