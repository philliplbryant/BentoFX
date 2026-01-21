import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.impl.storage.provider.DatabaseLayoutStorageProvider;

module bento.fx.persistence.storage.db {

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires jakarta.persistence;
    requires bento.fx.persistence.api;
    requires org.jetbrains.annotations;

    exports software.coley.bentofx.persistence.impl.storage.db;
    exports software.coley.bentofx.persistence.impl.storage.provider;

    provides LayoutStorageProvider with DatabaseLayoutStorageProvider;
}
