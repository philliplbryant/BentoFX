import org.jspecify.annotations.NullMarked;

/**
 * This module provides classes used by multiple implementations persisting
 * BentoFX layouts.
 *
 * @author Phil Bryant
 */
@NullMarked
module bento.fx.persistence.codec.common {

    requires transitive bento.fx.persistence.api;

    requires transitive javafx.graphics;

    requires org.slf4j;

    requires static org.jspecify;

    exports software.coley.bentofx.persistence.impl.codec.common.mapper;
    exports software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

    opens software.coley.bentofx.persistence.impl.codec.common.mapper.dto;
}
