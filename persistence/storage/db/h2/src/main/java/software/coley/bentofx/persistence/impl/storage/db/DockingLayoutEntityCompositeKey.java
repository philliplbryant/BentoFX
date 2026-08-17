package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.storage.LayoutIdentifiers;

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
     * The width of both identifier columns, taken from the rule every storage
     * implementation shares.
     *
     * <p>{@link LayoutIdentifiers#MAX_JOINED_LENGTH} is what the two identifiers
     * may take <em>together</em>, because file-backed storage joins them into one
     * path component. A column that wide therefore holds anything either of them
     * can validly be, and the number lives in one place rather than being
     * repeated here.</p>
     */
    public static final int MAX_COMPOSITE_KEY_LENGTH = LayoutIdentifiers.MAX_JOINED_LENGTH;

    public static final String LAYOUT_ID_COLUMN_NAME = "layout_id";

    public static final String CODEC_ID_COLUMN_NAME = "codec_id";

    @Column(name = LAYOUT_ID_COLUMN_NAME, nullable = false, length = MAX_COMPOSITE_KEY_LENGTH)
    public @Nullable String layoutIdentifier;

    @Column(name = CODEC_ID_COLUMN_NAME, nullable = false, length = MAX_COMPOSITE_KEY_LENGTH)
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
