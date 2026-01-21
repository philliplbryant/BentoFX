/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

module bento.fx.persistence.api {

    requires static org.jetbrains.annotations;

    requires java.desktop;
    requires javafx.graphics;
    requires bento.fx;

	exports software.coley.bentofx.persistence.api;
	exports software.coley.bentofx.persistence.api.codec;
    exports software.coley.bentofx.persistence.api.storage;
    exports software.coley.bentofx.persistence.api.provider;
}
