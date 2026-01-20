import software.coley.bentofx.persistence.impl.codec.provider.XmlLayoutCodecProvider;
import software.coley.bentofx.persistence.impl.storage.provider.FileLayoutStorageProvider;

module bento.fx.demo.application.main {

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

    // These packages must be exported for the JavaFX launcher to access the
    // application classes in them.
    exports software.coley.boxfx.demo;

    uses FileLayoutStorageProvider;
    uses XmlLayoutCodecProvider;
    uses software.coley.bentofx.persistence.api.storage.LayoutStorageProvider;
    uses software.coley.bentofx.persistence.api.codec.LayoutCodecProvider;
}
