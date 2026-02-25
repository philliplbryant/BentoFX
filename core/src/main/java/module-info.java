/**
 * A docking system for JavaFX.
 * <p>
 * Everything is open/exported. Do whatever you want.
 * </p>
 */
open module bento.fx {

    requires java.desktop;

    requires static transitive javafx.controls;

    requires static org.jetbrains.annotations;

	exports software.coley.bentofx;
	exports software.coley.bentofx.building;
	exports software.coley.bentofx.control;
	exports software.coley.bentofx.control.canvas;
	exports software.coley.bentofx.dockable;
	exports software.coley.bentofx.event;
	exports software.coley.bentofx.layout;
	exports software.coley.bentofx.layout.container;
	exports software.coley.bentofx.path;
	exports software.coley.bentofx.search;
	exports software.coley.bentofx.util;
}
