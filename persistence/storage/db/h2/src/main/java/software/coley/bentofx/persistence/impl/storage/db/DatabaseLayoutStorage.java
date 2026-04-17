package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger =
            LoggerFactory.getLogger(DatabaseLayoutStorage.class);

    private final EntityManagerFactory emf;
    private final String layoutIdentifier;
    private final String codecIdentifier;

    public DatabaseLayoutStorage(
            final EntityManagerFactory emf,
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        this.emf = emf;
        this.layoutIdentifier = layoutIdentifier;
        this.codecIdentifier = codecIdentifier;
    }

    @Override
    public boolean exists() {

        try (final EntityManager em = emf.createEntityManager()) {

            final DockingLayoutEntityCompositeKey key = new DockingLayoutEntityCompositeKey(
                            layoutIdentifier,
                            codecIdentifier
                    );

            final DockingLayoutEntity entity =
                    em.find(
                            DockingLayoutEntity.class,
                            key
                    );

            return entity.payload.length > 0;
        }
    }

    @Override
    public InputStream openInputStream() {

        try (final EntityManager em = emf.createEntityManager()) {

            logger.trace(
                    "Creating input stream using layout {} and codec {}.",
                    layoutIdentifier,
                    codecIdentifier
                    );

            final DockingLayoutEntityCompositeKey key =
                    new DockingLayoutEntityCompositeKey(
                            layoutIdentifier,
                            codecIdentifier
                    );

            final DockingLayoutEntity entity =
                    em.find(
                            DockingLayoutEntity.class,
                            key
                    );

            return new ByteArrayInputStream(entity.payload);
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

                        final DockingLayoutEntityCompositeKey key =
                                new DockingLayoutEntityCompositeKey(
                                        layoutIdentifier,
                                        codecIdentifier
                                );

                        final DockingLayoutEntity newEntity =
                                new DockingLayoutEntity();
                        newEntity.key = key;
                        newEntity.payload = bytesToSave;
                        newEntity.updatedAt = Instant.now();
                        em.persist(newEntity);

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
