package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for uniquely identifying docking layouts.
 *
 * @author Phil Bryant
 */
@Embeddable
public class DockingLayoutEntityCompositeKey implements Serializable {

    /**
     * The width of both identifier columns.
     *
     * <p>These identifiers may be used as a file name and its extension in
     * file-backed storage, so the width that matters is what a file name
     * allows, and every mainstream filesystem takes 255 characters per path
     * component.</p>
     */
    public static final int MAX_COMPOSITE_KEY_LENGTH = 255;

    @Column(name = "layout_id", nullable = false, length = MAX_COMPOSITE_KEY_LENGTH)
    public @Nullable String layoutIdentifier;

    @Column(name = "codec_id", nullable = false, length = MAX_COMPOSITE_KEY_LENGTH)
    public @Nullable String codecIdentifier;

    public DockingLayoutEntityCompositeKey() {}

    public DockingLayoutEntityCompositeKey(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        this.layoutIdentifier = Objects.requireNonNull(layoutIdentifier);
        this.codecIdentifier = Objects.requireNonNull(codecIdentifier);
    }

    @Override
    public boolean equals(final @Nullable Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof final DockingLayoutEntityCompositeKey thatKey)) {
            return false;
        }

        return Objects.equals(layoutIdentifier, thatKey.layoutIdentifier)
                && Objects.equals(codecIdentifier, thatKey.codecIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                layoutIdentifier,
                codecIdentifier
        );
    }
}
