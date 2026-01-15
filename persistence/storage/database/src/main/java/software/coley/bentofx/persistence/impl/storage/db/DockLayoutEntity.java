/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "dock_layout")
public class DockLayoutEntity {

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
