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

    // TODO BENTO-13: Add fields for the layout name and codec used to create it.

    @Id
    @Column(name = "layout_key", nullable = false, length = 128)
    public String key = "default";

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "payload", nullable = false)
    public byte[] payload = new byte[0];

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.EPOCH;
}
