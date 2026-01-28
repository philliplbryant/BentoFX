/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.*;

import java.time.Instant;

import static software.coley.bentofx.persistence.api.provider.LayoutStorageProvider.DEFAULT_LAYOUT_NAME;

/**
 * Represents a row in a table in a relational database for storing Bento
 * layouts.
 *
 * @author Phil Bryant
 */
@Entity
@Table(name = "docking_layout")
public class DockingLayoutEntity {

    // TODO BENTO-13: Create a compound key using both the layout_id and
    //  codec_id columns

    @Id
    @Column(name = "layout_id", nullable = false, length = 24)
    public String layoutIdentifier = DEFAULT_LAYOUT_NAME;

    @Column(name = "codec_id", nullable = false, length = 4)
    public String codecIdentifier = "default";

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "payload", nullable = false)
    public byte[] payload = new byte[0];

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.EPOCH;
}
