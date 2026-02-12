package software.coley.bentofx.persistence.api.provider;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@code Stage} icon {@link Image}s.
 *
 * @author Phil Bryant
 */
public interface StageIconImageProvider {

    /**
     * Returns a collection of varying sizes for the {@link Image} to be used
     * for {@code Stage} and {@code DragDropStage} instances.
     *
     * @return a collection of varying sizes for the {@link Image} to be used
     * for {@code Stage} and {@code DragDropStage} instances.
     */
    @NotNull Collection<@NotNull Image> getStageIcons();
}
