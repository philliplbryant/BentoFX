/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.provider.BentoLayoutPersistenceProvider;

/**
 * This module provides the persistence Application Programming Interface (API)
 * for the BentoFX docking framework.
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.api {

    uses LayoutCodecProvider;
    uses LayoutStorageProvider;

    requires transitive bento.fx;
    requires transitive javafx.graphics;

    requires static org.jetbrains.annotations;
    requires org.slf4j;

    exports software.coley.bentofx.persistence.api;
	exports software.coley.bentofx.persistence.api.codec;
    exports software.coley.bentofx.persistence.api.storage;
    exports software.coley.bentofx.persistence.api.provider;
    exports software.coley.bentofx.persistence.impl.provider;
    exports software.coley.bentofx.persistence.impl;

    provides LayoutPersistenceProvider with BentoLayoutPersistenceProvider;
}
