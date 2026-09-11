package software.coley.bentofx.building;

import software.coley.bentofx.control.ContentWrapper;
import software.coley.bentofx.control.HeaderPane;
import software.coley.bentofx.layout.container.DockContainerLeaf;

/**
 * Factory for building {@link ContentWrapper} in a parent {@link HeaderPane}.
 *
 * @author Matt Coley
 */
public interface ContentWrapperFactory {
	/**
	 * {@return newly created content wrapper}
	 *
	 * @param container
	 * 		Parent container.
	 */
	ContentWrapper newContentWrapper(DockContainerLeaf container);
}
