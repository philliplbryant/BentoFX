/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.provider;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.storage.db.DatabaseLayoutStorage;

/**
 * Implementation of the {@link LayoutStorageProvider} interface for persisting
 * Bento layouts to databases.
 *
 * @author Phil Bryant
 */
public class DatabaseLayoutStorageProvider implements LayoutStorageProvider {

    @Override
    public LayoutStorage createLayoutStorage(final @NotNull String codecIdentifier) {

        final EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("bentoLayout");

        return new DatabaseLayoutStorage(
                entityManagerFactory,
                codecIdentifier
        );
    }
}
