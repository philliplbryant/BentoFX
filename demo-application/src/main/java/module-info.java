import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.provider.*;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutRestorer;
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutSaver;
import software.coley.bentofx.persistence.impl.codec.common.provider.BentoLayoutPersistenceProvider;
import software.coley.bentofx.persistence.impl.codec.provider.XmlLayoutCodecProvider;
import software.coley.bentofx.persistence.impl.storage.provider.DatabaseLayoutStorageProvider;
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
module bento.fx.demo.application {

    requires static jakarta.annotation;
    requires static jakarta.inject;
    requires static org.jetbrains.annotations;

    requires java.logging;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    requires bento.fx;
    requires bento.fx.persistence.api;
    requires bento.fx.persistence.codec.common;
    requires org.slf4j;

    /////////////////////////////////////////////
    // Storage service provider implementation //
    /////////////////////////////////////////////

    // Database Storage Service Provider Implementation
    requires bento.fx.persistence.storage.db;
    requires com.zaxxer.hikari;
    requires jakarta.cdi.lang.model;
    requires jakarta.el;
    requires jakarta.persistence;
    requires jakarta.transaction;
    requires net.bytebuddy;
    requires org.hibernate.orm.hikaricp;
    requires org.hibernate.validator;
    requires org.jboss.logging;

//    // File Storage Service Provider Implementation
//    requires bento.fx.persistence.storage.file;

    ///////////////////////////////////////////
    // Codec service provider implementation //
    ///////////////////////////////////////////

//    // JSON Codec Service Provider Implementation
//    requires bento.fx.persistence.codec.json;

    // XML Codec Service Provider Implementation
    requires bento.fx.persistence.codec.xml;

    /////////////////
    // Public APIs //
    /////////////////

    // This must be exported for the JavaFX launcher to access the
    // application classes in them.
    exports software.coley.boxfx.demo;

    ///////////////////////
    // Reflective access //
    ///////////////////////
    opens software.coley.boxfx.demo to
            jakarta.persistence;

    //////////////////////////////////////
    // Service provider interfaces used //
    //////////////////////////////////////

    uses DockableProvider;
    uses ImageProvider;
    uses DockContainerLeafMenuFactoryProvider;
    uses DockableMenuFactoryProvider;
    uses LayoutPersistenceProvider;
    uses LayoutRestorer;
    uses LayoutSaver;
    uses LayoutStorageProvider;
    uses LayoutCodecProvider;

    ///////////////////////////////////////////
    // Service provider implementations used //
    ///////////////////////////////////////////

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

    //////////////////////////////////////////////////////
    // Codec service provider implementation to be used //
    //////////////////////////////////////////////////////

//    // LayoutCodecProvider
//    uses JsonLayoutCodecProvider;

    // LayoutCodecProvider
    uses XmlLayoutCodecProvider;

    ////////////////////////////////////////////////////////
    // Storage service provider implementation to be used //
    ////////////////////////////////////////////////////////

    // LayoutStorageProvider
    uses DatabaseLayoutStorageProvider;

//    // LayoutStorageProvider
//    uses FileLayoutStorageProvider;

    ///////////////////////////////////////////////
    // Service provider implementations provided //
    ///////////////////////////////////////////////

    provides DockableProvider with
            BoxAppDockableProvider;

    provides ImageProvider with
            BoxAppImageProvider;

    provides DockContainerLeafMenuFactoryProvider with
            BoxAppDockContainerLeafMenuFactoryProvider;

    provides DockableMenuFactoryProvider with
            BoxAppDockableMenuFactoryProvider;
}
