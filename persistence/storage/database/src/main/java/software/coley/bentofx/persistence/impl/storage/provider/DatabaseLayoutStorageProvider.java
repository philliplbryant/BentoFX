/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.storage.provider;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.storage.db.DatabaseLayoutStorage;

public class DatabaseLayoutStorageProvider implements LayoutStorageProvider {

    @Override
    public LayoutStorage createLayoutStorage(final @NotNull String fileExtension) {

        final EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("layoutPU");

        return new DatabaseLayoutStorage(
                entityManagerFactory,
                "default"
        );
    }
}
