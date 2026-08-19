package software.coley.bentofx.persistence.impl.storage.db.provider;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutIdentifiers;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.storage.db.DatabaseLayoutStorage;

import java.util.List;

/**
 * Implementation of the {@link LayoutStorageProvider} interface for persisting
 * Bento layouts to databases.
 *
 * <p>Every storage this hands out shares one {@link EntityManagerFactory},
 * created when the first one is requested. A factory starts Hibernate and brings
 * a connection pool with it, while the layouts it serves are one file that a
 * single connection would satisfy; what a saver and a restorer each need of
 * their own is the {@link LayoutStorage}, which is still built per call.</p>
 *
 * <p>Nothing closes the shared factory before the JVM exits, which is also when
 * the pool and the embedded database go. There is no earlier moment to close it
 * at: the storages are handed to components that outlive individual saves, and
 * {@link LayoutStorageProvider} has no shutdown of its own to hook into.</p>
 *
 * @author Phil Bryant
 */
public class DatabaseLayoutStorageProvider implements LayoutStorageProvider {

    private static final String IDENTIFIER = "h2";

    private static final String PERSISTENCE_UNIT_NAME = "bentoLayout";

    private @Nullable EntityManagerFactory entityManagerFactory;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        LayoutIdentifiers.requireValid(layoutIdentifier, codecIdentifier);

        return new DatabaseLayoutStorage(
                getEntityManagerFactory(),
                layoutIdentifier,
                codecIdentifier
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>One row per layout and codec, so the layouts stored are the layout
     * identifiers on rows with a payload.</p>
     */
    @Override
    public List<String> getLayoutIdentifiers(final String codecIdentifier) {
        return DatabaseLayoutStorage.getLayoutIdentifiers(
                getEntityManagerFactory(),
                codecIdentifier
        );
    }

    @Override
    public boolean deleteLayout(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        LayoutIdentifiers.requireValid(layoutIdentifier, codecIdentifier);

        return DatabaseLayoutStorage.deleteLayout(
                getEntityManagerFactory(),
                layoutIdentifier,
                codecIdentifier
        );
    }

    /**
     * {@return the shared {@link EntityManagerFactory}, creating it when this is the
     * first call to need it.}
     *
     * <p>Synchronized because the first caller is the one that creates it, and
     * nothing says a saver, a restorer and a catalog lookup happen on one
     * thread.</p>
     */
    private synchronized EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            entityManagerFactory =
                    Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }

        return entityManagerFactory;
    }
}
