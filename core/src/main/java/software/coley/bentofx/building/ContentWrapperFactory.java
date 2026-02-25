package software.coley.bentofx.building;

import org.jetbrains.annotations.NotNull;
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
	 * @param container
	 * 		Parent container.
	 *
	 * @return Newly created content wrapper.
	 */
	@NotNull
	ContentWrapper newContentWrapper(@NotNull DockContainerLeaf container);
}
