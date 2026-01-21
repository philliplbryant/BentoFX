import software.coley.bentofx.persistence.api.DockableProvider;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutRestorer;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutSaver;
import software.coley.bentofx.persistence.impl.codec.common.provider.BentoLayoutPersistenceProvider;
import software.coley.bentofx.persistence.impl.codec.provider.XmlLayoutCodecProvider;
import software.coley.bentofx.persistence.impl.storage.provider.FileLayoutStorageProvider;
import software.coley.boxfx.demo.provider.BoxAppDockableProvider;

module bento.fx.demo.application {

    requires static jakarta.annotation;
    requires static jakarta.inject;
    requires static org.jetbrains.annotations;

    requires java.logging;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    requires bento.fx;
    requires bento.fx.persistence.codec.common;
    requires bento.fx.persistence.codec.xml;
    requires bento.fx.persistence.storage.file;
    requires bento.fx.persistence.api;
    requires jakarta.persistence;
    requires org.slf4j;

    // These packages must be exported for the JavaFX launcher to access the
    // application classes in them.
    exports software.coley.boxfx.demo;

    provides DockableProvider with BoxAppDockableProvider;

    // Service provider interfaces
    uses DockableProvider;
    uses LayoutCodecProvider;
    uses LayoutPersistenceProvider;
    uses LayoutRestorer;
    uses LayoutSaver;
    uses LayoutStorageProvider;

    // Service provider implementations
    uses BoxAppDockableProvider;            // DockableProvider
    uses XmlLayoutCodecProvider;            // LayoutCodecProvider
    uses BentoLayoutPersistenceProvider;    // LayoutPersistenceProvider
    uses BentoLayoutRestorer;               // LayoutRestorer
    uses BentoLayoutSaver;                  // LayoutSaver
    uses FileLayoutStorageProvider;         // LayoutStorageProvider
}
