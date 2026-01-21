/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.boxfx.demo.provider;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.api.provider.DockableProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static javafx.scene.effect.BlurType.ONE_PASS_BOX;
import static javafx.scene.paint.Color.BLACK;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableProvider}.
 */
public class BoxAppDockableProvider implements DockableProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(BoxAppDockableProvider.class);

    private static final @NotNull List<@NotNull String> ICON_RESOURCES = List.of(
            "/images/logo-16.png",
            "/images/logo-32.png",
            "/images/logo-48.png",
            "/images/logo-256.png"
    );

    public static final @NotNull String WORKSPACE_DOCKABLE_ID = "Workspace";
    public static final @NotNull String BOOKMARKS_DOCKABLE_ID = "Bookmarks";
    public static final @NotNull String MODIFICATIONS_DOCKABLE_ID = "Modifications";
    public static final @NotNull String LOGGING_DOCKABLE_ID = "Logging";
    public static final @NotNull String TERMINAL_DOCKABLE_ID = "Terminal";
    public static final @NotNull String PROBLEMS_DOCKABLE_ID = "Problems";
    public static final @NotNull String CLASS_1_DOCKABLE_ID = "Class 1";
    public static final @NotNull String CLASS_2_DOCKABLE_ID = "Class 2";
    public static final @NotNull String CLASS_3_DOCKABLE_ID = "Class 3";
    public static final @NotNull String CLASS_4_DOCKABLE_ID = "Class 4";
    public static final @NotNull String CLASS_5_DOCKABLE_ID = "Class 5";

    private final @NotNull Map<@NotNull String, @NotNull Dockable> dockablesMap =
            new HashMap<>();

    public void init(final @NotNull DockBuilding builder) {

        // Initialization of these values must be performed on the JavaFX
        // Application Thread because they create JavaFX components.
        Platform.runLater(  () -> {
                    dockablesMap.put(
                            WORKSPACE_DOCKABLE_ID,
                            buildDockable(builder, 1, 0, WORKSPACE_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            BOOKMARKS_DOCKABLE_ID,
                            buildDockable(builder, 1, 1, BOOKMARKS_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            MODIFICATIONS_DOCKABLE_ID,
                            buildDockable(builder, 1, 2, MODIFICATIONS_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            LOGGING_DOCKABLE_ID,
                            buildDockable(builder, 2, 0, LOGGING_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            TERMINAL_DOCKABLE_ID,
                            buildDockable(builder, 2, 1, TERMINAL_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            PROBLEMS_DOCKABLE_ID,
                            buildDockable(builder, 2, 2, PROBLEMS_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            CLASS_1_DOCKABLE_ID,
                            buildDockable(builder, 0, 0, CLASS_1_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            CLASS_2_DOCKABLE_ID,
                            buildDockable(builder, 0, 1, CLASS_2_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            CLASS_3_DOCKABLE_ID,
                            buildDockable(builder, 0, 2, CLASS_3_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            CLASS_4_DOCKABLE_ID,
                            buildDockable(builder, 0, 3, CLASS_4_DOCKABLE_ID)
                    );
                    dockablesMap.put(
                            CLASS_5_DOCKABLE_ID,
                            buildDockable(builder, 0, 3, CLASS_5_DOCKABLE_ID)
                    );
                }
        );
    }

    @Override
    public Optional<@Nullable Dockable> resolveDockable(String id) {
        return Optional.ofNullable(dockablesMap.get(id));
    }

    // TODO BENTO-13: Move this to a different interface/implementation?
    @Override
    public @NotNull Collection<@NotNull Image> getDefaultStageIcons() {

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

    @NotNull
    private Dockable buildDockable(@NotNull DockBuilding builder, int s, int i, @NotNull String title) {
        Dockable dockable = builder.dockable(title);
        dockable.setTitle(title);
        dockable.setIconFactory(d -> makeIcon(s, i));
        dockable.setNode(new Label("<" + title + ":" + i + ">"));
        dockable.setContextMenuFactory(d -> {
            return new ContextMenu(
                    new MenuItem("Menu for : " + dockable.getTitle()),
                    new SeparatorMenuItem(),
                    new MenuItem("Stuff")
            );
        });
        if (s > 0) {
            dockable.setDragGroupMask(1);
            dockable.setClosable(false);
        }
        return dockable;
    }

    @NotNull
    private static Shape makeIcon(int shapeMode, int i) {
        final int radius = 6;
        Shape icon = switch (shapeMode) {
            case 1 -> new Polygon(
                    radius,
                    0,
                    0,
                    radius * 2,
                    radius * 2,
                    radius * 2
            );
            case 2 -> new Rectangle(radius * 2, radius * 2);
            default -> new Circle(radius);
        };
        switch (i) {
            case 0 -> icon.setFill(Color.RED);
            case 1 -> icon.setFill(Color.ORANGE);
            case 2 -> icon.setFill(Color.LIME);
            case 3 -> icon.setFill(Color.CYAN);
            case 4 -> icon.setFill(Color.BLUE);
            case 5 -> icon.setFill(Color.PURPLE);
            default -> icon.setFill(Color.GREY);
        }
        icon.setEffect(
                new InnerShadow(
                        ONE_PASS_BOX,
                        BLACK,
                        2F,
                        10F,
                        0,
                        0
                )
        );
        return icon;
    }
}
