/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2025 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.boxfx.demo.provider;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;
import software.coley.bentofx.persistence.api.provider.DockableMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javafx.scene.effect.BlurType.ONE_PASS_BOX;
import static javafx.scene.paint.Color.BLACK;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableProvider}.
 *
 * @author Phil Bryant
 */
public class BoxAppDockableProvider implements DockableProvider {

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

    @NotNull
    private final Map<@NotNull String, @NotNull Dockable> dockablesMap =
            new HashMap<>();

    public void init(
            final @NotNull DockBuilding builder,
            final @Nullable DockableMenuFactoryProvider dockableMenuFactoryProvider
    ) {

        // Initialization of these values must be performed on the JavaFX
        // Application Thread because they create JavaFX components.
        Platform.runLater(() -> {
                    dockablesMap.put(
                            WORKSPACE_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    1,
                                    0,
                                    WORKSPACE_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            BOOKMARKS_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    1,
                                    1,
                                    BOOKMARKS_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            MODIFICATIONS_DOCKABLE_ID,
                            buildDockable(
                                    builder, dockableMenuFactoryProvider,
                                    1,
                                    2,
                                    MODIFICATIONS_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            LOGGING_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    2,
                                    0,
                                    LOGGING_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            TERMINAL_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    2,
                                    1,
                                    TERMINAL_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            PROBLEMS_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    2,
                                    2,
                                    PROBLEMS_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            CLASS_1_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    0,
                                    0,
                                    CLASS_1_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            CLASS_2_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    0,
                                    1,
                                    CLASS_2_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            CLASS_3_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    0,
                                    2,
                                    CLASS_3_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            CLASS_4_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    0,
                                    3,
                                    CLASS_4_DOCKABLE_ID
                            )
                    );
                    dockablesMap.put(
                            CLASS_5_DOCKABLE_ID,
                            buildDockable(
                                    builder,
                                    dockableMenuFactoryProvider,
                                    0,
                                    4,
                                    CLASS_5_DOCKABLE_ID
                            )
                    );
                }
        );
    }

    @Override
    public Optional<@Nullable Dockable> resolveDockable(String id) {
        return Optional.ofNullable(dockablesMap.get(id));
    }

    @NotNull
    private Dockable buildDockable(
            @NotNull DockBuilding builder,
            @Nullable DockableMenuFactoryProvider dockableMenuFactoryProvider,
            int s,
            int i,
            @NotNull String title
    ) {
        Dockable dockable = builder.dockable(title);
        dockable.setTitle(title);
        // TODO BENTO-13: Use the ImageProvider to create these images
        dockable.setIconFactory(d -> makeIcon(s, i));
        dockable.setNode(new Label("<" + title + ":" + i + ">"));
        if (dockableMenuFactoryProvider != null) {
            final DockableMenuFactory dockableMenuFactory =
                    dockableMenuFactoryProvider.createDockableMenuFactory(dockable);
            if (dockableMenuFactory != null) {
                dockable.setContextMenuFactory(dockableMenuFactory);
            }
        }
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
            case 2 -> new Rectangle(radius * 2d, radius * 2d);
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
