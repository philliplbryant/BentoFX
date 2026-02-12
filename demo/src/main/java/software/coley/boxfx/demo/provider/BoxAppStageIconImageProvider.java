package software.coley.boxfx.demo.provider;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link StageIconImageProvider}.
 *
 * @author Phil Bryant
 */
public class BoxAppStageIconImageProvider implements StageIconImageProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(BoxAppStageIconImageProvider.class);

    private static final @NotNull List<@NotNull String> ICON_RESOURCES = List.of(
            "/images/logo-16.png",
            "/images/logo-32.png",
            "/images/logo-48.png",
            "/images/logo-256.png"
    );

    @Override
    public @NotNull Collection<@NotNull Image> getStageIcons() {

        final List<@NotNull Image> images = new ArrayList<>();

        for (final String iconResource : ICON_RESOURCES) {
            try (
                    final InputStream inputStream =
                            getClass().getResourceAsStream(iconResource)
            ) {
                if (inputStream == null) {

                    logger.warn(
                            "Could not find the resource {}.", iconResource
                    );
                } else {

                    images.add(new Image(inputStream));
                }
            } catch (IOException e) {

                logger.warn(
                        "Could not read the resource {}.", iconResource,
                        e
                );
            }
        }

        return images;
    }
}
