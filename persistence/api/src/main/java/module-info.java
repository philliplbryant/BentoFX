import org.jspecify.annotations.NullMarked;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.provider.DockingLayoutPersistenceProvider;

/**
 * This module provides the persistence Application Programming Interface (API)
 * for the BentoFX docking framework.
 *
 * @author Phil Bryant
 */
@NullMarked
module bento.fx.persistence.api {

    uses LayoutCodecProvider;
    uses LayoutStorageProvider;

    requires transitive bento.fx;
    requires transitive javafx.graphics;

    requires static org.jspecify;

    requires javafx.controls;
    requires org.slf4j;

	exports software.coley.bentofx.persistence.api.codec;
    exports software.coley.bentofx.persistence.api.provider;
    exports software.coley.bentofx.persistence.api.storage;
    exports software.coley.bentofx.persistence.api;
    exports software.coley.bentofx.persistence.impl.codec;
    exports software.coley.bentofx.persistence.impl.provider;
    exports software.coley.bentofx.persistence.impl;

    provides LayoutPersistenceProvider with DockingLayoutPersistenceProvider;
}
