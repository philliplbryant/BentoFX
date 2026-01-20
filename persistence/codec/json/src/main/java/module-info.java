module bento.fx.persistence.codec.json {

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires org.jetbrains.annotations;
    requires bento.fx.persistence.api;
    requires bento.fx.persistence.codec.common;

	exports software.coley.bentofx.persistence.impl.codec.json;
}
