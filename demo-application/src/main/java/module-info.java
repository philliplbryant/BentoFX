import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.provider.*;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutRestorer;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutSaver;
import software.coley.bentofx.persistence.impl.codec.common.provider.BentoLayoutPersistenceProvider;
import software.coley.bentofx.persistence.impl.codec.provider.XmlLayoutCodecProvider;
import software.coley.bentofx.persistence.impl.storage.provider.FileLayoutStorageProvider;
import software.coley.boxfx.demo.provider.BoxAppDockContainerLeafMenuFactoryProvider;
import software.coley.boxfx.demo.provider.BoxAppDockableMenuFactoryProvider;
import software.coley.boxfx.demo.provider.BoxAppDockableProvider;
import software.coley.boxfx.demo.provider.BoxAppImageProvider;

/**
 * This module is a very basic JavaFX application demonstrating the BentoFX
 * docking framework and persistence API.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
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

// TODO BENTO-13: Specify the storage and codec provider modules that are required.

//    requires bento.fx.persistence.codec.json;
    requires bento.fx.persistence.storage.file;
//    requires bento.fx.persistence.storage.db;
    requires bento.fx.persistence.api;
    requires jakarta.persistence;
    requires org.slf4j;

    // These packages must be exported for the JavaFX launcher to access the
    // application classes in them.
    exports software.coley.boxfx.demo;

    provides DockableProvider with BoxAppDockableProvider;
    provides ImageProvider with BoxAppImageProvider;
    provides DockContainerLeafMenuFactoryProvider with BoxAppDockContainerLeafMenuFactoryProvider;
    provides DockableMenuFactoryProvider with BoxAppDockableMenuFactoryProvider;

    // Service provider interfaces
    uses DockableProvider;
    uses ImageProvider;
    uses DockContainerLeafMenuFactoryProvider;
    uses DockableMenuFactoryProvider;
    uses LayoutPersistenceProvider;
    uses LayoutRestorer;
    uses LayoutSaver;
    uses LayoutStorageProvider;
    uses LayoutCodecProvider;

    // Service provider implementations
    uses BoxAppDockableProvider;                        // DockableProvider
    uses BoxAppImageProvider;                           // ImageProvider
    uses BoxAppDockContainerLeafMenuFactoryProvider;    // DockContainerLeafMenuFactoryProvider
    uses BoxAppDockableMenuFactoryProvider;             // DockableMenuFactoryProvider
    uses BentoLayoutPersistenceProvider;                // LayoutPersistenceProvider
    uses BentoLayoutRestorer;                           // LayoutRestorer
    uses BentoLayoutSaver;                              // LayoutSaver

// TODO BENTO-13: Specify the storage and codec modules that are to be used.

    uses FileLayoutStorageProvider;                     // LayoutStorageProvider
//    uses DatabaseLayoutStorageProvider;                  // LayoutStorageProvider
    uses XmlLayoutCodecProvider;                        // LayoutCodecProvider
//    uses JsonLayoutCodecProvider;                     // LayoutCodecProvider
}
