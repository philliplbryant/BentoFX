/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.jetbrains.annotations.NotNull;
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

    private final @NotNull EntityManagerFactory emf;
    private final @NotNull String codecIdentifier;

    public DatabaseLayoutStorage(
            final @NotNull  EntityManagerFactory emf,
            final @NotNull String codecIdentifier
    ) {
        this.emf = emf;
        this.codecIdentifier = codecIdentifier;
    }

    @Override
    public boolean exists() {

        try (final EntityManager em = emf.createEntityManager()) {
            final DockLayoutEntity entity =
                    em.find(DockLayoutEntity.class, codecIdentifier);
            return entity != null && entity.payload != null && entity.payload.length > 0;
        }
    }

    @Override
    public InputStream openInputStream() {
        try (final EntityManager em = emf.createEntityManager()) {
            final DockLayoutEntity entity =
                    em.find(DockLayoutEntity.class, codecIdentifier);
            final byte[] bytes =
                    (entity != null && entity.payload != null) ?
                            entity.payload :
                            new byte[0];
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

                        final DockLayoutEntity existing =
                                em.find(
                                        DockLayoutEntity.class,
                                        codecIdentifier
                                );
                        if (existing == null) {
                            final DockLayoutEntity newEntity =
                                    new DockLayoutEntity();
                            newEntity.codecIdentifier = codecIdentifier;
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
