package software.coley.bentofx.building;

import software.coley.bentofx.control.canvas.PixelCanvas;
import software.coley.bentofx.layout.container.DockContainerLeaf;

/**
 * Factory for building a {@link PixelCanvas} for a {@link DockContainerLeaf}.
 *
 * @author Matt Coley
 */
public interface CanvasFactory {
	/**
	 * {@return new canvas}
	 *
	 * @param container
	 * 		Parent container.
	 */
	PixelCanvas newCanvas(DockContainerLeaf container);
}
