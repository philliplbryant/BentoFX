module bento.fx.demo.application.main {

    requires static jakarta.annotation;

    requires java.logging;
    requires javafx.controls;
    requires kotlin.stdlib;
    requires org.jetbrains.annotations;

    requires bento.fx;
    requires bento.fx.persistence.codec.common;
    requires bento.fx.persistence.codec.json;
    requires bento.fx.persistence.codec.xml;
    requires bento.fx.persistence.storage.db;
    requires bento.fx.persistence.storage.file;
}
