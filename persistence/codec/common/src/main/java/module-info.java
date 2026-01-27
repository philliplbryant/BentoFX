/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.impl.codec.common.provider.BentoLayoutPersistenceProvider;

/**
 * This module provides classes used by multiple implementations persisting
 * BentoFX layouts.
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.codec.common {

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive jakarta.xml.bind;
    requires transitive javafx.controls;

    requires bento.fx.persistence.api;

    requires org.slf4j;

    requires static jakarta.annotation;
    requires static org.jetbrains.annotations;

	exports software.coley.bentofx.persistence.impl.codec.common;
	exports software.coley.bentofx.persistence.impl.codec.common.mapper;
    exports software.coley.bentofx.persistence.impl.codec.common.mapper.dto;
    exports software.coley.bentofx.persistence.impl.codec.common.provider;

    opens software.coley.bentofx.persistence.impl.codec.common.mapper.dto
            to jakarta.xml.bind;


    provides LayoutPersistenceProvider with BentoLayoutPersistenceProvider;
}
