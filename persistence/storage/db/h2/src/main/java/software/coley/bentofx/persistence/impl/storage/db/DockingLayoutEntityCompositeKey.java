package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DockingLayoutEntityCompositeKey implements Serializable {

    @Column(name = "layout_id", nullable = false, length = 24)
    public String layoutIdentifier;

    @Column(name = "codec_id", nullable = false, length = 4)
    public String codecIdentifier;

    public DockingLayoutEntityCompositeKey() {}

    public DockingLayoutEntityCompositeKey(
            final @NotNull String layoutIdentifier,
            final @NotNull String codecIdentifier
    ) {
        this.layoutIdentifier = Objects.requireNonNull(layoutIdentifier);
        this.codecIdentifier = Objects.requireNonNull(codecIdentifier);
    }

    @Override
    public boolean equals(Object that) {

        if (this == that) return true;

        if (that == null || this.getClass() != that.getClass()) return false;

        DockingLayoutEntityCompositeKey thatKey =
                (DockingLayoutEntityCompositeKey) that;

        return Objects.equals(
                this.layoutIdentifier,
                thatKey.layoutIdentifier
        ) && Objects.equals(
                this.codecIdentifier,
                thatKey.codecIdentifier
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                layoutIdentifier,
                codecIdentifier
        );
    }
}
