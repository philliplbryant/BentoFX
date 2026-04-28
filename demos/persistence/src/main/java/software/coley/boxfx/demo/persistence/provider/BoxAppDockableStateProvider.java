package software.coley.boxfx.demo.persistence.provider;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableMenuFactory;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.impl.codec.DockableState;
import software.coley.bentofx.persistence.impl.codec.DockableState.DockableStateBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javafx.scene.effect.BlurType.ONE_PASS_BOX;
import static javafx.scene.paint.Color.BLACK;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableStateProvider}.
 *
 * @author Phil Bryant
 */
public class BoxAppDockableStateProvider implements DockableStateProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(BoxAppDockableStateProvider.class);


    public static final DockableProperties workspaceDockableProperties = new DockableProperties(
            "Workspace",
            "This is the Workspace tooltip text."
    );

    public static final DockableProperties bookmarksDockableProperties = new DockableProperties(
            "Bookmarks",
            "This is the Bookmarks tooltip text."
    );

    public static final DockableProperties modificationsDockableProperties = new DockableProperties(
            "Modifications",
            "This is the Modifications tooltip text."
    );

    public static final DockableProperties loggingDockableProperties = new DockableProperties(
            "Logging",
            "This is the Logging tooltip text."
    );

    public static final DockableProperties terminalDockableProperties = new DockableProperties(
            "Terminal",
            "This is the Terminal tooltip text."
    );

    public static final DockableProperties problemsDockableProperties = new DockableProperties(
            "Problems",
            "This is the Problems tooltip text."
    );

    public static final DockableProperties class1DockableProperties = new DockableProperties(
            "Class 1",
            "This is the Class 1 tooltip text."
    );

    public static final DockableProperties class2DockableProperties = new DockableProperties(
            "Class 2",
            "This is the Class 2 tooltip text."
    );

    public static final DockableProperties class3DockableProperties = new DockableProperties(
            "Class 3",
            "This is the Class 3 tooltip text."
    );

    public static final DockableProperties class4DockableProperties = new DockableProperties(
            "Class 4",
            "This is the Class 4 tooltip text."
    );

    public static final DockableProperties class5DockableProperties = new DockableProperties(
            "Class 5",
            "This is the Class 5 tooltip text."
    );

    public static final DockableProperties secondDockableProperties = new DockableProperties(
            "second-dockable",
            "This is the Second Dockable tooltip text."
            );


    private final Map<String, DockableState> dockablesMap =
            new HashMap<>();

    /**
     * Creates a {@code BoxAppDockableStateProvider}.
     *
     * @param dockableMenuFactory {@link DockableMenuFactory} to use to create
     *                            {@code ContextMenu} instances.
     */
    public BoxAppDockableStateProvider(
            final @Nullable DockableMenuFactory dockableMenuFactory
    ) {
        // Initialization of these values must be performed on the JavaFX
        // Application Thread because they create JavaFX components.
        Platform.runLater(() -> {
                    dockablesMap.put(
                            workspaceDockableProperties.identifier(),
                            buildDockableState(
                                    workspaceDockableProperties,
                                    dockableMenuFactory,
                                    1,
                                    0
                            )
                    );
                    dockablesMap.put(
                            bookmarksDockableProperties.identifier(),
                            buildDockableState(
                                    bookmarksDockableProperties,
                                    dockableMenuFactory,
                                    1,
                                    1
                            )
                    );
                    dockablesMap.put(
                            modificationsDockableProperties.identifier(),
                            buildDockableState(
                                    modificationsDockableProperties,
                                    dockableMenuFactory,
                                    1,
                                    2
                            )
                    );
                    dockablesMap.put(
                            loggingDockableProperties.identifier(),
                            buildDockableState(
                                    loggingDockableProperties,
                                    dockableMenuFactory,
                                    2,
                                    0
                            )
                    );
                    dockablesMap.put(
                            terminalDockableProperties.identifier(),
                            buildDockableState(
                                    terminalDockableProperties,
                                    dockableMenuFactory,
                                    2,
                                    1
                            )
                    );
                    dockablesMap.put(
                            problemsDockableProperties.identifier(),
                            buildDockableState(
                                    problemsDockableProperties,
                                    dockableMenuFactory,
                                    2,
                                    2
                            )
                    );
                    dockablesMap.put(
                            class1DockableProperties.identifier(),
                            buildDockableState(
                                    class1DockableProperties,
                                    dockableMenuFactory,
                                    0,
                                    0
                            )
                    );
                    dockablesMap.put(
                            class2DockableProperties.identifier(),
                            buildDockableState(
                                    class2DockableProperties,
                                    dockableMenuFactory,
                                    0,
                                    1
                            )
                    );
                    dockablesMap.put(
                            class3DockableProperties.identifier(),
                            buildDockableState(
                                    class3DockableProperties,
                                    dockableMenuFactory,
                                    0,
                                    2
                            )
                    );
                    dockablesMap.put(
                            class4DockableProperties.identifier(),
                            buildDockableState(
                                    class4DockableProperties,
                                    dockableMenuFactory,
                                    0,
                                    3
                            )
                    );
                    dockablesMap.put(
                            class5DockableProperties.identifier(),
                            buildDockableState(
                                    class5DockableProperties,
                                    dockableMenuFactory,
                                    0,
                                    4
                            )
                    );
                    dockablesMap.put(
                            secondDockableProperties.identifier(),
                            createSecondDockableState()
                    );
                }
        );
    }

    @Override
    public Optional<DockableState> resolveDockableState(
            String id
    ) {
        return Optional.ofNullable(dockablesMap.get(id));
    }

    private DockableState buildDockableState(
            DockableProperties dockableProperties,
            @Nullable DockableMenuFactory dockableMenuFactory,
            int s,
            int i
    ) {
        DockableStateBuilder builder = new DockableStateBuilder(dockableProperties.identifier())
                .setTitle(dockableProperties.identifier())
                .setTooltip(dockableProperties.tooltip())
                .setDockableIconFactory(dockable -> makeIcon(s, i))
                .setDockableNode(new Label("<" + dockableProperties.identifier() + ":" + i + ">"))
                .setDockableConsumer(
                        BoxAppDockableStateProvider::consumeDockable
                ).setDockableMenuFactory(dockableMenuFactory);


        if (s > 0) {
            builder.setDragGroupMask(1);
            builder.setClosable(false);
        }

        return builder.build();
    }

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

    private DockableState createSecondDockableState() {

        return new DockableStateBuilder(secondDockableProperties.identifier())
                .setTitle(secondDockableProperties.identifier())
                .setTooltip(secondDockableProperties.tooltip())
                .setDockableNode(new Label("<" + secondDockableProperties.identifier() + ">"))
                .setDockableConsumer(
                        BoxAppDockableStateProvider::consumeDockable
                ).build();
    }


    /**
     * Callback function executed when the {@link Dockable} is created from the
     * {@link DockableState} returned by {@link #buildDockableState}.
     *
     * @param dockable the {@link Dockable} created from the
     *                 {@link DockableState} returned by {@link #buildDockableState}.
     */
    private static void consumeDockable(Dockable dockable) {
        logger.debug("Consuming dockable {}", dockable);
    }
}
