import org.jspecify.annotations.NullMarked;
import software.coley.bentofx.persistence.core.api.provider.DockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.core.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.core.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.core.impl.provider.DefaultDockingLayoutPersistenceProvider;

/**
 * This module provides the persistence Application Programming Interface (API)
 * for the BentoFX docking framework.
 *
 * @author Phil Bryant
 */
@NullMarked
module bento.fx.persistence.core {

    uses LayoutCodecProvider;
    uses LayoutStorageProvider;

    /*
     * This module both provides and uses the persistence provider, which is
     * deliberate: DockingLayoutPersistence.provider() performs the lookup here so
     * that consumers do not have to. Without this clause that call fails with a
     * ServiceConfigurationError, and every consuming application would need a
     * 'uses' clause of its own instead.
     */
    uses DockingLayoutPersistenceProvider;

    requires transitive bento.fx;
    requires transitive javafx.graphics;

    requires static org.jspecify;

    requires javafx.controls;
    requires org.slf4j;

    exports software.coley.bentofx.persistence.core.api.codec;
    exports software.coley.bentofx.persistence.core.api.provider;
    exports software.coley.bentofx.persistence.core.api.storage;
    exports software.coley.bentofx.persistence.core.api;
    exports software.coley.bentofx.persistence.core.api.state;

    /*
     * None of the impl packages are exported. Everything in them is an implementation
     * detail reachable through the api packages above, and exporting them makes
     * classes in them part of this module's compatibility surface - which defeats
     * both the api/impl split and the point of routing construction through
     * DockingLayoutPersistenceProvider.
     *
     * The provides clause here does not need its package exported: the module
     * system instantiates a service implementation reflectively. Consumers obtain
     * the provider through DockingLayoutPersistence.provider().
     */
    provides DockingLayoutPersistenceProvider with DefaultDockingLayoutPersistenceProvider;
}
