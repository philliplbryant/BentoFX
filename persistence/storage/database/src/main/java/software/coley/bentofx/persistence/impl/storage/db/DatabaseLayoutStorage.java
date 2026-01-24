/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.*;
import java.time.Instant;

/**
 * Implementation of the {@link LayoutStorage} interface for persisting Bento
 * layouts to a database using a Jakarta {@link EntityManagerFactory}.
 *
 * @author Phil Bryant
 */
public class DatabaseLayoutStorage implements LayoutStorage {

    private final EntityManagerFactory emf;
    private final String layoutKey;

    public DatabaseLayoutStorage(
            final EntityManagerFactory emf,
            final String layoutKey
    ) {
        this.emf = emf;
        this.layoutKey = layoutKey != null ? layoutKey : "default";
    }

    @Override
    public boolean exists() {

        try (final EntityManager em = emf.createEntityManager()) {
            final DockLayoutEntity entity = em.find(DockLayoutEntity.class, layoutKey);
            return entity != null && entity.payload != null && entity.payload.length > 0;
        }
    }

    @Override
    public InputStream openInputStream() {
        try (final EntityManager em = emf.createEntityManager()) {
            final DockLayoutEntity entity = em.find(DockLayoutEntity.class, layoutKey);
            final byte[] bytes = (entity != null && entity.payload != null) ? entity.payload : new byte[0];
            return new ByteArrayInputStream(bytes);
        }
    }

    @Override
    public OutputStream openOutputStream() {
        // Capture bytes, then persist on close()
        return new ByteArrayOutputStream() {
            private boolean closed = false;

            @Override
            public void close() throws IOException {

                if (!closed) {
                    closed = true;
                    super.close();

                    final byte[] bytesToSave = this.toByteArray();

                    final EntityManager em = emf.createEntityManager();
                    final EntityTransaction tx = em.getTransaction();

                    try (em) {
                        tx.begin();

                        final DockLayoutEntity existing = em.find(DockLayoutEntity.class, layoutKey);
                        if (existing == null) {
                            final DockLayoutEntity newEntity = new DockLayoutEntity();
                            newEntity.key = layoutKey;
                            newEntity.payload = bytesToSave;
                            newEntity.updatedAt = Instant.now();
                            em.persist(newEntity);
                        } else {
                            existing.payload = bytesToSave;
                            existing.updatedAt = Instant.now();
                            em.merge(existing);
                        }

                        tx.commit();
                    } catch (final Exception e) {
                        if (tx.isActive()) {
                            tx.rollback();
                        }
                        throw new IOException(
                                "Could not close output stream.",
                                e
                        );
                    }
                }
            }
        };
    }
}
