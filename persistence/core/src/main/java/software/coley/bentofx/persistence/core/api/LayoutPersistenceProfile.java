package software.coley.bentofx.persistence.core.api;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Identifies the layout to save or restore and, optionally, the codec and
 * storage provider implementations to use and the human-readable name to store
 * with it.
 *
 * <p>A profile with only a layout identifier allows the persistence framework
 * to select codec and storage providers from the runtime dependencies.
 * Supplying codec and/or storage identifiers allows applications to select
 * specific providers when multiple implementations are available.</p>
 *
 * @param layoutIdentifier stable identifier for the saved layout
 * @param codecIdentifier optional codec provider identifier
 * @param storageIdentifier optional storage provider identifier
 * @param displayName optional human-readable name stored with the layout
 * @param group optional group the layout belongs to, {@code null} when it
 * belongs to none
 * @author Phil Bryant
 */
public record LayoutPersistenceProfile(
        String layoutIdentifier,
        @Nullable String codecIdentifier,
        @Nullable String storageIdentifier,
        @Nullable String displayName,
        @Nullable String group
) {

    /**
     * Canonical constructor.
     */
    public LayoutPersistenceProfile {
        Objects.requireNonNull(layoutIdentifier, "layoutIdentifier");
    }

    /**
     * Create a profile with explicit codec and storage selection and no display
     * name.
     *
     * @param layoutIdentifier stable identifier for the saved layout
     * @param codecIdentifier codec provider identifier
     * @param storageIdentifier storage provider identifier
     */
    public LayoutPersistenceProfile(
            final String layoutIdentifier,
            final @Nullable String codecIdentifier,
            final @Nullable String storageIdentifier
    ) {
        this(layoutIdentifier, codecIdentifier, storageIdentifier, null, null);
    }

    /**
     * Create a profile carrying a display name but no group.
     *
     * @param layoutIdentifier stable identifier for the saved layout
     * @param codecIdentifier optional codec provider identifier
     * @param storageIdentifier optional storage provider identifier
     * @param displayName optional human-readable name stored with the layout
     */
    public LayoutPersistenceProfile(
            final String layoutIdentifier,
            final @Nullable String codecIdentifier,
            final @Nullable String storageIdentifier,
            final @Nullable String displayName
    ) {
        this(
                layoutIdentifier,
                codecIdentifier,
                storageIdentifier,
                displayName,
                null
        );
    }

    /**
     * Create a profile that lets the framework choose codec and storage
     * providers from runtime dependencies.
     *
     * @param layoutIdentifier stable identifier for the saved layout
     * @return layout persistence profile
     */
    public static LayoutPersistenceProfile of(final String layoutIdentifier) {
        return new LayoutPersistenceProfile(
                layoutIdentifier,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Create a profile that carries a display name to store with the layout.
     *
     * <p>The caller supplies the layout identifier; this does not derive one
     * from the display name. Turning a user's chosen name into a valid
     * identifier is a separate step; see
     * {@link software.coley.bentofx.persistence.core.api.storage.LayoutIdentifiers}
     * for what makes an identifier usable.</p>
     *
     * @param layoutIdentifier stable identifier for the saved layout
     * @param displayName human-readable name to store with the layout
     * @param codecIdentifier optional codec provider identifier
     * @param storageIdentifier optional storage provider identifier
     * @return layout persistence profile
     */
    public static LayoutPersistenceProfile named(
            final String layoutIdentifier,
            final String displayName,
            final @Nullable String codecIdentifier,
            final @Nullable String storageIdentifier
    ) {
        Objects.requireNonNull(displayName, "displayName");

        return new LayoutPersistenceProfile(
                layoutIdentifier,
                codecIdentifier,
                storageIdentifier,
                displayName,
                null
        );
    }

    /**
     * {@return this profile with a different display name and group, and the
     * same selections otherwise.}
     *
     * <p>What a rename and a move to another group are asked for with. Both
     * values are stored as given, so a {@code null} clears rather than
     * leaving what is stored alone.</p>
     *
     * @param displayName the name to store, or {@code null} to store none.
     * @param group the group to store, or {@code null} for no group.
     */
    public LayoutPersistenceProfile withNaming(
            final @Nullable String displayName,
            final @Nullable String group
    ) {
        return new LayoutPersistenceProfile(
                layoutIdentifier,
                codecIdentifier,
                storageIdentifier,
                displayName,
                group
        );
    }

    /**
     * {@return this profile's display name, if it carries one.}
     */
    public Optional<String> findDisplayName() {
        return Optional.ofNullable(displayName);
    }

    /**
     * {@return the group this profile's layout belongs to, if it belongs to
     * one.}
     */
    public Optional<String> findGroup() {
        return Optional.ofNullable(group);
    }
}
