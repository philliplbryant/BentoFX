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
import software.coley.bentofx.persistence.api.provider.DockableMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.state.DockableState;
import software.coley.bentofx.persistence.api.state.DockableState.DockableStateBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javafx.scene.effect.BlurType.ONE_PASS_BOX;
import static javafx.scene.paint.Color.BLACK;
import static software.coley.boxfx.demo.persistence.provider.DockableProperties.*;

/**
 * {@code ServiceLoader} compatible Service Provider implementation of
 * {@link DockableStateProvider}.
 *
 * @author Phil Bryant
 */
public class BoxAppDockableStateProvider implements DockableStateProvider {

	private static final Logger logger =
			LoggerFactory.getLogger(BoxAppDockableStateProvider.class);

	/**
	 * Maps {@link DockableState} to {@link Dockable} identifier.
	 */
	private final Map<String, DockableState> dockableStateMap =
			new HashMap<>();

	/**
	 * Creates a {@code BoxAppDockableStateProvider}.
	 *
	 * @param dockableMenuFactoryProvider {@link DockableMenuFactory} to use to create
	 * {@code ContextMenu} instances.
	 */
	public BoxAppDockableStateProvider(
			final @Nullable DockableMenuFactoryProvider dockableMenuFactoryProvider
	) {
		// Initialization of these values must be performed on the JavaFX
		// Application Thread because they create JavaFX components.
		Platform.runLater(() -> {
					// Create static DockableState and add them to a map so they can be
					// retrieved using their identifiers.
					dockableStateMap.put(
							WORKSPACE.getIdentifier(),
							buildDockableState(
									WORKSPACE,
									dockableMenuFactoryProvider,
									1,
									0
							)
					);
					dockableStateMap.put(
							BOOKMARKS.getIdentifier(),
							buildDockableState(
									BOOKMARKS,
									dockableMenuFactoryProvider,
									1,
									1
							)
					);
					dockableStateMap.put(
							MODIFICATIONS.getIdentifier(),
							buildDockableState(
									MODIFICATIONS,
									dockableMenuFactoryProvider,
									1,
									2
							)
					);
					dockableStateMap.put(
							LOGGING.getIdentifier(),
							buildDockableState(
									LOGGING,
									dockableMenuFactoryProvider,
									2,
									0
							)
					);
					dockableStateMap.put(
							TERMINAL.getIdentifier(),
							buildDockableState(
									TERMINAL,
									dockableMenuFactoryProvider,
									2,
									1
							)
					);
					dockableStateMap.put(
							PROBLEMS.getIdentifier(),
							buildDockableState(
									PROBLEMS,
									dockableMenuFactoryProvider,
									2,
									2
							)
					);
					dockableStateMap.put(
							CLASS_1.getIdentifier(),
							buildDockableState(
									CLASS_1,
									dockableMenuFactoryProvider,
									0,
									0
							)
					);
					dockableStateMap.put(
							CLASS_2.getIdentifier(),
							buildDockableState(
									CLASS_2,
									dockableMenuFactoryProvider,
									0,
									1
							)
					);
					dockableStateMap.put(
							CLASS_3.getIdentifier(),
							buildDockableState(
									CLASS_3,
									dockableMenuFactoryProvider,
									0,
									2
							)
					);
					dockableStateMap.put(
							CLASS_4.getIdentifier(),
							buildDockableState(
									CLASS_4,
									dockableMenuFactoryProvider,
									0,
									3
							)
					);
					dockableStateMap.put(
							CLASS_5.getIdentifier(),
							buildDockableState(
									CLASS_5,
									dockableMenuFactoryProvider,
									0,
									4
							)
					);
					dockableStateMap.put(
							SOMETHING_ELSE.getIdentifier(),
							createSecondDockableState()
					);
				}
		);
	}

	@Override
	public Optional<DockableState> resolveDockableState(
			String id
	) {
		return Optional.ofNullable(dockableStateMap.get(id));
	}

	private DockableState buildDockableState(
			DockableProperties dockableProperties,
			@Nullable DockableMenuFactoryProvider dockableMenuFactoryProvider,
			int s,
			int i
	) {
		final String dockableIdentifier = dockableProperties.getIdentifier();
		final String dockableTooltipText = dockableProperties.getTooltipText();
		final DockableMenuFactory dockableMenuFactory =
				dockableMenuFactoryProvider == null ?
						null :
						dockableMenuFactoryProvider
						.getDockableMenuFactory(dockableIdentifier)
						.orElse(null);

		DockableStateBuilder builder = new DockableStateBuilder(dockableProperties.getIdentifier())
				.setTitle(dockableIdentifier)
				.setTooltipText(dockableTooltipText)
				.setDockableIconFactory(dockable -> makeIcon(s, i))
				.setDockableNode(new Label("<" + dockableIdentifier + ":" + i + ">"))
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

		return new DockableStateBuilder(SOMETHING_ELSE.getIdentifier())
				.setTitle(SOMETHING_ELSE.getIdentifier())
				.setTooltipText(SOMETHING_ELSE.getTooltipText())
				.setDockableNode(new Label("<" + SOMETHING_ELSE.getIdentifier() + ">"))
				.setDockableConsumer(
						BoxAppDockableStateProvider::consumeDockable
				).build();
	}

	/**
	 * Callback function executed when the {@link Dockable} is created from the
	 * {@link DockableState} returned by {@link #buildDockableState}.
	 *
	 * @param dockable the {@link Dockable} created from the
	 * {@link DockableState} returned by {@link #buildDockableState}.
	 */
	private static void consumeDockable(Dockable dockable) {
		logger.debug("Consuming dockable {}", dockable);
	}
}
