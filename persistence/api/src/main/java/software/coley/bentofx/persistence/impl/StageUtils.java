package software.coley.bentofx.persistence.impl;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

/**
 * Utility class containing methods relating to JavaFX {@link Stage}.
 *
 * @author Phil Bryant
 */
public class StageUtils {

    private StageUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns an immutable {@link List} containing all {@link Stage} instances.
     *
     * @return an immutable {@link List} containing all {@link Stage} instances.
     */
    public static List<Stage> getAllStages() {
        return Window.getWindows()
                .stream()
                .filter(
                        Stage.class::isInstance
                )
                .map(
                        Stage.class::cast
                )
                .toList();
    }

    /**
     * Calculates the horizontal position of a {@link Stage}, bounded within the
     * horizontal boundary of all {@link Screen}.
     * @param stage the {@link Stage} whose horizontally bounded {@link Screen}
     *              position is to the determined.
     * @param stageX the unbounded horizontal position of the {@link Stage}.
     * @return the horizontal position of a {@link Stage}, bounded within the
     * horizontal boundary of all {@link Screen}.
     */
    public static double getXInScreenBounds(
            final Stage stage,
            final Double stageX
    ) {
        final Rectangle2D allScreenBounds = getAllScreenBounds();
        double boundedX = stageX;
        if (stageX < allScreenBounds.getMinX()) {
            // Ensure the X coordinate is not left of the minimum allowed X
            boundedX = allScreenBounds.getMinX();
        } else if (stageX + stage.getWidth() > allScreenBounds.getMaxX()) {
            // Ensure that the stage doesn't extend beyond the maximum X boundary
            boundedX = allScreenBounds.getMaxX() - stage.getWidth();
        }
        return boundedX;
    }

    /**
     * Calculates the vertical position of a {@link Stage}, bounded within the
     * vertical boundary of all {@link Screen}.
     * @param stage the {@link Stage} whose vertically bounded {@link Screen}
     *              position is to be determined.
     * @param stageY the unbounded vertical position of the {@link Stage}.
     * @return the vertical position of a {@link Stage}, bounded within the
     * vertical boundary of all {@link Screen}.
     */
    public static double getYInScreenBounds(
            final Stage stage,
            final Double stageY
    ) {
        final Rectangle2D allScreenBounds = getAllScreenBounds();

        double boundedY = stageY; // Start with the unbounded Y position

        if (stageY < allScreenBounds.getMinY()) {
            // Ensure the Y coordinate is not above the minimum allowed Y
            boundedY = allScreenBounds.getMinY();
        }
        else if (stageY + stage.getHeight() > allScreenBounds.getMaxY()) {
            // Ensure that the stage doesn't extend beyond the maximum Y boundary
            boundedY = allScreenBounds.getMaxY() - stage.getHeight();
        }

        return boundedY; // Return the adjusted Y position
    }

    /**
     * Calculates the boundary for all {@link Screen}.
     * @return the boundary for all {@link Screen}.
     */
    private static Rectangle2D getAllScreenBounds() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        // Loop through all available screens and calculate the boundary
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            minX = Math.min(bounds.getMinX(), minX);
            minY = Math.min(bounds.getMinY(), minY);
            maxX = Math.max(bounds.getMaxX(), maxX);
            maxY = Math.max(bounds.getMaxY(), maxY);
        }

        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }
}
