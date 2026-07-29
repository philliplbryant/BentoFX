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

    @Column(name = "layout_id", nullable = false, length = 24)
    public @Nullable String layoutIdentifier;

    @Column(name = "codec_id", nullable = false, length = 4)
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
