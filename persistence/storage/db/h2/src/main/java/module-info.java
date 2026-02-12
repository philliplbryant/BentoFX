/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.storage.db.provider.DatabaseLayoutStorageProvider;

/**
 * This module implements the Bento persistence database storage provider using
 * an H2 database configured using the Jakarta persistence API with Hikari for
 * thread pooling.
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.storage.db.h2Database {

    requires transitive bento.fx.persistence.api;

    requires transitive jakarta.persistence;

    requires static org.hibernate.orm.core;

    requires static org.jetbrains.annotations;

    requires com.zaxxer.hikari;
    requires jakarta.cdi.lang.model;
    requires jakarta.el;
    requires jakarta.transaction;
    requires net.bytebuddy;
    requires org.hibernate.orm.hikaricp;
    requires org.hibernate.validator;
    requires org.slf4j;

    exports software.coley.bentofx.persistence.impl.storage.db;
    exports software.coley.bentofx.persistence.impl.storage.db.provider;

    opens software.coley.bentofx.persistence.impl.storage.db to
            org.hibernate.orm.core;

    provides LayoutStorageProvider with
            DatabaseLayoutStorageProvider;
}
