package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Represents a row in a table in a relational database for storing Bento
 * layouts.
 *
 * @author Phil Bryant
 */
@Entity
@Table(name = "docking_layout")
public class DockingLayoutEntity {

    @EmbeddedId
    public DockingLayoutEntityCompositeKey key;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "payload", nullable = false)
    public byte[] payload = new byte[0];

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.EPOCH;
}
