/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.storage.provider.DatabaseLayoutStorageProvider;

/**
 * This module implements the Application Programming Interface (API) for
 * encoding and decoding the layout of BentoFX docking framework components
 * using a database.
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.storage.db {

    requires transitive bento.fx.persistence.api;

    requires transitive jakarta.persistence;

    requires static org.hibernate.orm.core;

    requires static org.jetbrains.annotations;

    exports software.coley.bentofx.persistence.impl.storage.db;
    exports software.coley.bentofx.persistence.impl.storage.provider;

    opens software.coley.bentofx.persistence.impl.storage.db to
            org.hibernate.orm.core;

    provides LayoutStorageProvider with
            DatabaseLayoutStorageProvider;
}
