/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.json.provider.JsonLayoutCodecProvider;

/**
 * This module implements the Application Programming Interface (API) for
 * encoding and decoding the layout of BentoFX docking framework components
 * using JavaScript Object Notation (JSON).
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.codec.json {

    requires transitive bento.fx.persistence.api;

    requires bento.fx.persistence.codec.common;

    requires com.fasterxml.jackson.databind;

    requires static org.jetbrains.annotations;

	exports software.coley.bentofx.persistence.impl.codec.json;
    exports software.coley.bentofx.persistence.impl.codec.json.provider;

    provides LayoutCodecProvider with JsonLayoutCodecProvider;
}
