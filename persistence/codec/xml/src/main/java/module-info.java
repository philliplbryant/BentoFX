import software.coley.bentofx.persistence.api.codec.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.provider.XmlLayoutCodecProvider;

module bento.fx.persistence.codec.xml {

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires jakarta.xml.bind;
    requires org.jetbrains.annotations;
    requires bento.fx.persistence.api;
    requires bento.fx.persistence.codec.common;

	exports software.coley.bentofx.persistence.impl.codec.xml;
    exports software.coley.bentofx.persistence.impl.codec.provider;

    provides LayoutCodecProvider with XmlLayoutCodecProvider;
}
