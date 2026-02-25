import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;

/**
 * This module is a very basic JavaFX application demonstrating the BentoFX
 * docking framework and the persistence API with provided implementations.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
module bento.fx.demo {

    requires bento.fx;
    requires bento.fx.persistence.api;

    requires java.logging;
    requires javafx.controls;
    requires org.slf4j;

    requires static org.jetbrains.annotations;

    // This must be exported for the JavaFX launcher to access the
    // application classes in them.
    exports software.coley.boxfx.demo;

    uses LayoutPersistenceProvider;
}
