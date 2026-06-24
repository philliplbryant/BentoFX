package software.coley.bentofx.persistence.api;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Identifies the layout to save or restore and, optionally, the codec and
 * storage provider implementations to use.
 * <p>
 * A profile with only a layout identifier allows the persistence framework to
 * select codec and storage providers from the runtime dependencies. Supplying
 * codec and/or storage identifiers allows applications to select specific
 * providers when multiple implementations are available.
 *
 * @param layoutIdentifier stable identifier for the saved layout
 * @param codecIdentifier optional codec provider identifier
 * @param storageIdentifier optional storage provider identifier
 * @author Phil Bryant
 */
public record LayoutPersistenceProfile(
        String layoutIdentifier,
        @Nullable String codecIdentifier,
        @Nullable String storageIdentifier
) {

    /**
     * Create a profile with explicit codec and storage selection.
     *
     * @param layoutIdentifier stable identifier for the saved layout
     * @param codecIdentifier codec provider identifier
     * @param storageIdentifier storage provider identifier
     */
    public LayoutPersistenceProfile {
        Objects.requireNonNull(layoutIdentifier, "layoutIdentifier");
    }

    /**
     * Create a profile that lets the framework choose codec and storage
     * providers from runtime dependencies.
     *
     * @param layoutIdentifier stable identifier for the saved layout
     * @return layout persistence profile
     */
    public static LayoutPersistenceProfile of(final String layoutIdentifier) {
        return new LayoutPersistenceProfile(layoutIdentifier, null, null);
    }
}
