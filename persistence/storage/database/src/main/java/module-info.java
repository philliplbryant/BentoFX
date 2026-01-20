module bento.fx.persistence.storage.db {

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires jakarta.persistence;
    requires bento.fx.persistence.api;

    exports software.coley.bentofx.persistence.impl.storage.db;
}
