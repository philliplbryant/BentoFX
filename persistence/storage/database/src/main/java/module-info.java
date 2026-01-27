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

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires jakarta.persistence;
    requires bento.fx.persistence.api;
    requires org.jetbrains.annotations;
    requires org.hibernate.orm.core;

    exports software.coley.bentofx.persistence.impl.storage.db;
    exports software.coley.bentofx.persistence.impl.storage.provider;

    opens software.coley.bentofx.persistence.impl.storage.db to org.hibernate.orm.core;

    provides LayoutStorageProvider with DatabaseLayoutStorageProvider;
}
