/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

/**
 * This module provides the persistence Application Programming Interface (API)
 * for the BentoFX docking framework.
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.api {

    requires transitive bento.fx;
    requires transitive javafx.graphics;

    requires static org.jetbrains.annotations;

	exports software.coley.bentofx.persistence.api;
	exports software.coley.bentofx.persistence.api.codec;
    exports software.coley.bentofx.persistence.api.storage;
    exports software.coley.bentofx.persistence.api.provider;
}
