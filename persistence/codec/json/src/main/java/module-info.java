/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.provider.JsonLayoutCodecProvider;

/**
 * This module implements the Application Programming Interface (API) for
 * encoding and decoding the layout of BentoFX docking framework components
 * using JavaScript Object Notation (JSON).
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.codec.json {

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires org.jetbrains.annotations;
    requires bento.fx.persistence.api;
    requires bento.fx.persistence.codec.common;

	exports software.coley.bentofx.persistence.impl.codec.json;
    exports software.coley.bentofx.persistence.impl.provider;

    provides LayoutCodecProvider with JsonLayoutCodecProvider;
}
