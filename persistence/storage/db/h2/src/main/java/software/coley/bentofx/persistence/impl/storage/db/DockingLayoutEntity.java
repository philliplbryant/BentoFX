package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * Represents a row in a table in a relational database for storing Bento
 * layouts.
 *
 * @author Phil Bryant
 */
@Entity
@Table(name = DockingLayoutEntity.TABLE_NAME)
public class DockingLayoutEntity {

    public static final String TABLE_NAME = "docking_layout";

    public static final String PAYLOAD_COLUMN_NAME = "payload";

    @EmbeddedId
    public @Nullable DockingLayoutEntityCompositeKey key;

    @Lob
    @Column(name = PAYLOAD_COLUMN_NAME, nullable = false)
    public byte[] payload = new byte[0];

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.EPOCH;
}
