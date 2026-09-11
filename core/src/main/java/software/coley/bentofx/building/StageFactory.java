package software.coley.bentofx.building;

import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.control.DragDropStage;

/**
 * Factory for building new {@link DragDropStage}.
 *
 * @author Matt Coley
 */
public interface StageFactory {
	/**
	 * {@return newly created stage}
	 *
	 * @param sourceStage
	 * 		Original stage to copy state from.
	 */
	DragDropStage newStage(@Nullable Stage sourceStage);
}
