package software.coley.bentofx.persistence.core.api.provider;

import software.coley.bentofx.persistence.core.api.storage.LayoutStorage;

import java.util.List;

/**
 * Supplies the {@link LayoutStorage} that decides where a docking layout is
 * written to.
 *
 * <p>Discovered at runtime, so an application changes where layouts are kept by
 * changing which storage implementation it depends on - see
 * {@link LayoutPersistenceComponentProvider}. The provider is the discoverable
 * type; the {@link LayoutStorage} it returns does not need to be discoverable
 * itself.</p>
 *
 * <p>Beyond handing out storage for one layout, a provider can answer what its
 * destination holds: {@link #getLayoutIdentifiers(String)},
 * {@link #isLayoutStored(String, String)} and
 * {@link #deleteLayout(String, String)} are what an application needs to offer
 * users a list of saved layouts and let them remove one. All three have defaults,
 * so an implementation that cannot enumerate or delete stays valid, and
 * applications reach them through
 * {@link DockingLayoutPersistenceProvider} rather than selecting a storage
 * provider themselves.</p>
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

    /**
     * {@return the identifiers of the layouts this destination holds for the
     * supplied codec, in no particular order.}
     *
     * <p>A layout appears only when there is something to read: an entry with no
     * content is not a layout, which is the same rule {@link LayoutStorage#exists()}
     * applies.</p>
     *
     * <p>The default returns an empty list, which is also what a destination with
     * no layouts returns. An implementation that cannot enumerate is therefore
     * indistinguishable from an empty one, and both are equally uninteresting to a
     * caller building a menu. Implementations that can enumerate should.</p>
     *
     * <p>An unreachable destination throws an unchecked exception rather than
     * reporting no layouts, for the reason {@link LayoutStorage#exists()} gives:
     * answering "nothing here" when the truth is "cannot tell" invites the caller
     * to write over a layout that exists.</p>
     *
     * @param codecIdentifier identifies the codec whose layouts are wanted.
     */
    default List<String> getLayoutIdentifiers(final String codecIdentifier) {
        return List.of();
    }

    /**
     * {@return {@code true} when this destination holds a layout for the supplied
     * identifiers; otherwise, {@code false}.}
     *
     * <p>The default asks the storage itself, which every implementation can
     * answer. Override when the destination can answer more cheaply than opening
     * one.</p>
     *
     * @param layoutIdentifier identifies the layout.
     * @param codecIdentifier identifies the codec whose output is stored.
     */
    default boolean isLayoutStored(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        try (final LayoutStorage layoutStorage =
                     getLayoutStorage(layoutIdentifier, codecIdentifier)) {
            return layoutStorage.exists();
        }
    }

    /**
     * Removes the stored layout, if there is one.
     *
     * @param layoutIdentifier identifies the layout to remove.
     * @param codecIdentifier identifies the codec whose output is stored.
     * @return {@code true} when a layout was removed; {@code false} when there was
     * nothing to remove, or when this implementation does not support removal,
     * which is what the default reports.
     */
    default boolean deleteLayout(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        return false;
    }
}
