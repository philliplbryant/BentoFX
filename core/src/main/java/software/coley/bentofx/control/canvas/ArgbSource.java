package software.coley.bentofx.control.canvas;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * ARGB source to wrap arbitrary inputs representing image data.
 *
 * @author Matt Coley.
 */
public interface ArgbSource {
	/**
	 * {@return image width}
	 */
	int getWidth();

	/**
	 * {@return image height}
	 */
	int getHeight();

	/**
	 * {@return ARGB {@code int} at coordinate, defaulting to {@code 0} for any coordinate out of
	 * the image bounds}
	 *
	 * @param x
	 * 		Image X coordinate.
	 * @param y
	 * 		Image X coordinate.
	 */
	int getArgb(int x, int y);

	/**
	 * {@return ARGB {@code int[]} at coordinates for the given width/height, or
	 * {@code null} when coordinates are out of the image bounds}
	 *
	 * @param x
	 * 		Image X coordinate.
	 * @param y
	 * 		Image X coordinate.
	 * @param width
	 * 		Width of image section to grab.
	 * @param height
	 * 		Height of image section to grab.
	 */
	int @Nullable [] getArgb(int x, int y, int width, int height);

	/**
	 * {@return ARGB {@code int[]} for the full image}
	 */
	default int[] getArgb() {
		return Objects.requireNonNull(getArgb(0, 0, getWidth(), getHeight()),
				"Failed computing ARGB for full image dimensions");
	}
}
