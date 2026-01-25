package software.coley.bentofx.persistence.api.provider;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ImageProvider {

    /**
     * Returns a collection of varying sizes for the default {@link Image} to be
     * used for {@code Stage} and {@code DragDropStage} instances.
     *
     * @return a collection of varying sizes for the default {@link Image} to be
     * used for {@code Stage} and {@code DragDropStage} instances.
     */
    @NotNull Collection<@NotNull Image> getDefaultStageIcons();
}
