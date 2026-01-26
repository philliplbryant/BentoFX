/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Represents a row in a table in a relational database for storing Bento
 * layouts.
 *
 * @author Phil Bryant
 */
@Entity
@Table(name = "dock_layout")
public class DockLayoutEntity {

    // TODO BENTO-13: Add fields for the saved layout name (currently only one,
    //  see FileLayoutStorageProvider#DEFAULT_BENTO_FILE_NAME)

    @Id
    @Column(name = "codec_id", nullable = false, length = 128)
    public String codecIdentifier = "default";

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "payload", nullable = false)
    public byte[] payload = new byte[0];

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.EPOCH;
}
