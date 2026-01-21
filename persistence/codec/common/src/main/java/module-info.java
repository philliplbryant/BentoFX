import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.impl.codec.common.provider.BentoLayoutPersistenceProvider;

module bento.fx.persistence.codec.common {

	requires static jakarta.annotation;

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
    requires org.jetbrains.annotations;
    requires bento.fx.persistence.api;
    requires com.fasterxml.jackson.annotation;
    requires jakarta.xml.bind;
    requires org.slf4j;
    requires bento.fx;

	exports software.coley.bentofx.persistence.impl.codec.common;
	exports software.coley.bentofx.persistence.impl.codec.common.mapper;
    exports software.coley.bentofx.persistence.impl.codec.common.mapper.dto;

    opens software.coley.bentofx.persistence.impl.codec.common.mapper.dto
            to jakarta.xml.bind;
    exports software.coley.bentofx.persistence.impl.codec.common.provider;

    provides LayoutPersistenceProvider with BentoLayoutPersistenceProvider;
}
