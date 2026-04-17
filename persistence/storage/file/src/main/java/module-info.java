import org.jspecify.annotations.NullMarked;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.storage.provider.FileLayoutStorageProvider;

/**
 * This module implements the Application Programming Interface (API) for
 * encoding and decoding the layout of BentoFX docking framework components
 * files.
 *
 * @author Phil Bryant
 */
@NullMarked
module bento.fx.persistence.storage.file {

    requires transitive bento.fx.persistence.api;

    requires transitive java.logging;

    requires static org.jspecify;

    exports software.coley.bentofx.persistence.impl.storage.file;
    exports software.coley.bentofx.persistence.impl.storage.provider;

    provides LayoutStorageProvider with FileLayoutStorageProvider;
}
