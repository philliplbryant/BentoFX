package software.coley.bentofx.event;

import org.jetbrains.annotations.NotNull;

/**
 * Listener invoked by the firing of any {@link DockEvent}.
 *
 * @author Matt Coley
 */
public interface DockEventListener {
	/**
	 * @param event
	 * 		Event fired.
	 */
	void onDockEvent(@NotNull DockEvent event);
}
