module bento.fx.persistence.api {

    requires static org.jetbrains.annotations;

    requires java.desktop;
    requires javafx.graphics;
    requires bento.fx;

	exports software.coley.bentofx.persistence.api;
	exports software.coley.bentofx.persistence.api.codec;
	exports software.coley.bentofx.persistence.api.storage;
}
