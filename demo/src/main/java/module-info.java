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
 * docking framework and the persistence API with provided implementations.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
module bento.fx.demo {

    // <editor-fold desc="Module dependencies">
    requires bento.fx;

    requires org.slf4j;

    requires static bento.fx.persistence.codec.common;

    requires static jakarta.annotation;
    requires static org.jetbrains.annotations;
    // </editor-fold>

    // <editor-fold desc="Storage service provider implementation">

    // <editor-fold desc="Database">
//    requires bento.fx.persistence.storage.db.h2Database;
//    uses DatabaseLayoutStorageProvider;
    // </editor-fold>

    // <editor-fold desc="File">
    requires bento.fx.persistence.storage.file;
    uses FileLayoutStorageProvider;
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Codec service provider implementation">

    // <editor-fold desc="JSON">
//    requires bento.fx.persistence.codec.json;
//    uses JsonLayoutCodecProvider;
    // </editor-fold>

    // <editor-fold desc="XML">
    requires bento.fx.persistence.codec.xml;
    uses XmlLayoutCodecProvider;
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Public APIs">

    // This must be exported for the JavaFX launcher to access the
    // application classes in them.
    exports software.coley.boxfx.demo;

    // </editor-fold>

    // <editor-fold desc="Reflective access">

    opens software.coley.boxfx.demo to
            jakarta.persistence;

    // </editor-fold>

    // <editor-fold desc="Service provider interfaces used">

    uses DockableProvider;
    uses ImageProvider;
    uses DockContainerLeafMenuFactoryProvider;
    uses DockableMenuFactoryProvider;
    uses LayoutPersistenceProvider;
    uses LayoutRestorer;
    uses LayoutSaver;
    uses LayoutStorageProvider;
    uses LayoutCodecProvider;

    // </editor-fold>

    // <editor-fold desc="Service provider implementations used">

    // DockableProvider
    uses BoxAppDockableProvider;
    // ImageProvider
    uses BoxAppImageProvider;
    // DockContainerLeafMenuFactoryProvider
    uses BoxAppDockContainerLeafMenuFactoryProvider;
    // DockableMenuFactoryProvider
    uses BoxAppDockableMenuFactoryProvider;
    // LayoutPersistenceProvider
    uses BentoLayoutPersistenceProvider;
    // LayoutRestorer
    uses BentoLayoutRestorer;
    // LayoutSaver
    uses BentoLayoutSaver;

    // </editor-fold>

    // <editor-fold desc="Service provider implementations provided">

    provides DockableProvider with
            BoxAppDockableProvider;

    provides ImageProvider with
            BoxAppImageProvider;

    provides DockContainerLeafMenuFactoryProvider with
            BoxAppDockContainerLeafMenuFactoryProvider;

    provides DockableMenuFactoryProvider with
            BoxAppDockableMenuFactoryProvider;

    // </editor-fold>
}
