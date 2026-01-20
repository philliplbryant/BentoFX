module bento.fx.demo.application.main {

    requires static jakarta.annotation;
    requires static jakarta.inject;

    requires java.logging;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires kotlin.stdlib;
    requires org.jetbrains.annotations;

    requires bento.fx;
    requires bento.fx.persistence.codec.common;
    requires bento.fx.persistence.codec.json;
    requires bento.fx.persistence.codec.xml;
    requires bento.fx.persistence.storage.db;
    requires bento.fx.persistence.storage.file;

    // These packages must be exported for the JavaFX launcher (and Spring?) to
    // access it the application classes in them.
    exports software.coley.boxfx.demo;
}
