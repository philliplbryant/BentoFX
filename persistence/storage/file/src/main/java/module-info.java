/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.storage.provider.FileLayoutStorageProvider;

module bento.fx.persistence.storage.file {

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires bento.fx.persistence.api;
    requires org.jetbrains.annotations;

    exports software.coley.bentofx.persistence.impl.storage.file;
    exports software.coley.bentofx.persistence.impl.storage.provider;

    provides LayoutStorageProvider with FileLayoutStorageProvider;
}
