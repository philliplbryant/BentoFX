/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.provider.XmlLayoutCodecProvider;

/**
 * This module implements the Application Programming Interface (API) for
 * encoding and decoding the layout of BentoFX docking framework components
 * using the eXtensible Markup Language (XML).
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.codec.xml {

    requires transitive bento.fx.persistence.api;

    requires bento.fx.persistence.codec.common;

    requires jakarta.xml.bind;

    requires static org.jetbrains.annotations;

	exports software.coley.bentofx.persistence.impl.codec.xml;
    exports software.coley.bentofx.persistence.impl.codec.provider;

    provides LayoutCodecProvider with XmlLayoutCodecProvider;
}
