/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

/**
 * This module provides classes used by multiple implementations persisting
 * BentoFX layouts.
 *
 * @author Phil Bryant
 */
module bento.fx.persistence.codec.common {

    requires transitive bento.fx.persistence.api;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive jakarta.xml.bind;
    requires transitive javafx.controls;

    requires org.slf4j;

    requires static jakarta.annotation;
    requires static org.jetbrains.annotations;

	exports software.coley.bentofx.persistence.impl.codec.common.mapper;
    exports software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

    opens software.coley.bentofx.persistence.impl.codec.common.mapper.dto
            to jakarta.xml.bind;
}
