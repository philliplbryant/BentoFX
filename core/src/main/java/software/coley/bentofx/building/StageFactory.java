package software.coley.bentofx.building;

import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.control.DragDropStage;

/**
 * Factory for building new {@link DragDropStage}.
 *
 * @author Matt Coley
 */
public interface StageFactory {
	/**
	 * @param sourceStage
	 * 		Original stage to copy state from.
	 *
	 * @return Newly created stage.
	 */
	@NotNull
	DragDropStage newStage(@Nullable Stage sourceStage);
}
