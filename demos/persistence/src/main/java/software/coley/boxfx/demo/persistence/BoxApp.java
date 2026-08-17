package software.coley.boxfx.demo.persistence;

import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.event.DockEvent;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoLayout;
import software.coley.bentofx.persistence.api.BentoLayout.BentoLayoutBuilder;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.DockingLayout;
import software.coley.bentofx.persistence.api.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.api.DockingLayoutPersistence;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.DockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.state.DockableState;
import software.coley.boxfx.demo.persistence.provider.BoxAppDockContainerLeafMenuFactoryProvider;
import software.coley.boxfx.demo.persistence.provider.BoxAppDockableMenuFactoryProvider;
import software.coley.boxfx.demo.persistence.provider.BoxAppDockableStateProvider;
import software.coley.boxfx.demo.persistence.provider.BoxAppStageIconImageProvider;
import software.coley.boxfx.demo.persistence.provider.DockableProperties;

import java.util.ArrayList;
import java.util.List;

import static software.coley.boxfx.demo.persistence.provider.DockableProperties.*;

/**
 * JavaFX application that demonstrates using the BentoFX docking and docking
 * persistence frameworks. Derived from the {@code BoxApp} class in the basic
 * demo.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
public class BoxApp extends Application {

	private static final Logger logger =
			LoggerFactory.getLogger(BoxApp.class);

	private static final String DEFAULT_LAYOUT_IDENTIFIER = "recent";

	private final Bento bento = new Bento("box-app-bento");

	/**
	 * The root branches of the layout this demo builds for itself, which is what
	 * {@link #getDefaultDockingLayout()} hands to the restorer for when there is
	 * nothing to restore.
	 *
	 * <p>Not the input to a save. A capture reads the root branches each
	 * {@link Bento} knows about, which are the ones that have a {@link Scene}, so
	 * after a layout is restored and applied the branch in here is not the branch
	 * that gets persisted.</p>
	 */
	private final List<DockContainerRootBranch> defaultRootBranches =
			new ArrayList<>();

	private final DockingLayoutPersistenceProvider persistenceProvider =
			DockingLayoutPersistence.provider();

	private final DockableStateProvider dockableStateProvider =
			new BoxAppDockableStateProvider(
					new BoxAppDockableMenuFactoryProvider()
			);

	private final StageIconImageProvider stageIconImageProvider =
			new BoxAppStageIconImageProvider();

	private final DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider =
			new BoxAppDockContainerLeafMenuFactoryProvider();

	private final BentoProvider bentoProvider = BentoProvider.of(bento);

	private @Nullable Stage stage;

	private @Nullable LayoutSaver layoutSaver;

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		stage.setWidth(1000);
		stage.setHeight(700);

		// Initialize the Bento
		bento.placeholderBuilding().setDockablePlaceholderFactory(dockable -> new Label("Empty Dockable"));
		bento.placeholderBuilding().setContainerPlaceholderFactory(container -> new Label("Empty Container"));
		bento.events().addEventListener((DockEvent event) -> {
			if (event instanceof DockEvent.DockableClosing closingEvent)
				handleDockableClosing(closingEvent);
		});
		bento.stageBuilding().setApplyMousePosition(true);
		bento.stageBuilding().setApplySourceAsOwner(false);

		final DockBuilding builder = bento.dockBuilding();
		final DockContainerRootBranch branchRoot = builder.root("root");
		final DockContainerBranch branchWorkspace = builder.branch("workspace");
		final DockContainerLeaf leafWorkspaceTools = builder.leaf("workspace-tools");
		final DockContainerLeaf leafWorkspaceHeaders = builder.leaf("workspace-headers");
		final DockContainerLeaf leafTools = builder.leaf("misc-tools");

		branchWorkspace.setPruneWhenEmpty(false);
		leafWorkspaceTools.setPruneWhenEmpty(false);
		leafTools.setPruneWhenEmpty(false);

		// Add dummy menus to each.
		dockContainerLeafMenuFactoryProvider.getDockContainerLeafMenuFactory(
				leafTools.getIdentifier()
		).ifPresent(leafTools::setMenuFactory);
		dockContainerLeafMenuFactoryProvider.getDockContainerLeafMenuFactory(
				leafWorkspaceHeaders.getIdentifier()
		).ifPresent(leafWorkspaceHeaders::setMenuFactory);
		dockContainerLeafMenuFactoryProvider.getDockContainerLeafMenuFactory(
				leafWorkspaceTools.getIdentifier()
		).ifPresent(leafWorkspaceTools::setMenuFactory);

		// These leaves shouldn't auto-expand. They are intended to be a set size.
		SplitPane.setResizableWithParent(leafTools, false);
		SplitPane.setResizableWithParent(leafWorkspaceTools, false);

		// Root: Workspace on top, tools on bottom
		// Workspace: Explorer on left, primary editor tabs on right
		branchRoot.setOrientation(Orientation.VERTICAL);
		branchWorkspace.setOrientation(Orientation.HORIZONTAL);
		branchRoot.addContainers(branchWorkspace, leafTools);
		branchWorkspace.addContainers(leafWorkspaceTools, leafWorkspaceHeaders);

		// Changing tool header sides to be aligned with application's far edges (to facilitate better collapsing UX)
		leafWorkspaceTools.setSide(Side.LEFT);
		leafTools.setSide(Side.BOTTOM);

		// Tools shouldn't allow splitting (mirroring IntelliJ behavior)
		leafWorkspaceTools.setCanSplit(false);
		leafTools.setCanSplit(false);

		// Primary editor space should not prune when empty
		leafWorkspaceHeaders.setPruneWhenEmpty(false);

		// Set intended sizes for tools (leaf does not need to be a direct child, just some level down in the chain)
		branchRoot.setContainerSizePx(leafTools, 200);
		branchRoot.setContainerSizePx(leafWorkspaceTools, 300);

		// Make the bottom collapsed by default
		branchRoot.setContainerCollapsed(leafTools, true);

		// Adding dockables to leafWorkspaceTools
		addDockable(WORKSPACE, dockableStateProvider, leafWorkspaceTools);
		addDockable(BOOKMARKS, dockableStateProvider, leafWorkspaceTools);
		addDockable(MODIFICATIONS, dockableStateProvider, leafWorkspaceTools);

		// Adding dockables to leafTools
		addDockable(LOGGING, dockableStateProvider, leafTools);
		addDockable(TERMINAL, dockableStateProvider, leafTools);
		addDockable(PROBLEMS, dockableStateProvider, leafTools);

		// Adding dockables to leafWorkspaceHeaders
		addDockable(CLASS_1, dockableStateProvider, leafWorkspaceHeaders);
		addDockable(CLASS_2, dockableStateProvider, leafWorkspaceHeaders);
		addDockable(CLASS_3, dockableStateProvider, leafWorkspaceHeaders);
		addDockable(CLASS_4, dockableStateProvider, leafWorkspaceHeaders);
		addDockable(CLASS_5, dockableStateProvider, leafWorkspaceHeaders);

		defaultRootBranches.add(branchRoot);

		stage.setTitle("BentoFX Persistence Demo");
		stage.getIcons().addAll(
				stageIconImageProvider.getStageIcons()
		);
		stage.centerOnScreen();

		// We need to save the docking layout on close request because the stage
		// is (and all other windows are) no longer available after they are
		// closed and will not be discoverable when saving the docking layout.
		stage.setOnCloseRequest(this::saveDockingLayout);
		stage.setOnHidden(e -> System.exit(0));

		// A Scene is created and additional Stage properties are set when
		// applying the docking layout.
		DockingLayout dockingLayout = getDockingLayout();

		if (!applyDockingLayout(dockingLayout)) {
			// Nothing was applied, so the stage has no Scene and was never shown.
			// Falling back to the default layout keeps the application usable and
			// leaves the reason in the log; without it a saved layout this demo
			// cannot apply leaves a running process with no window.
			logger.warn(
					"Could not apply the restored docking layout; " +
							"applying the default docking layout instead."
			);

			if (!applyDockingLayout(getDefaultDockingLayout())) {
				logger.error("Could not apply the default docking layout.");
			}
		}

		// Built last: a capture only sees root branches that have a Scene, and
		// auto-save starts as soon as the saver exists.
		layoutSaver = createLayoutSaver();
	}

	/**
	 * Builds and returns the {@link Dockable} for the specified
	 * {@link DockableState}.
	 *
	 * @param dockableState the {@link DockableState} specifying the state of
	 * the {@link Dockable} to be built.
	 *
	 * @return the {@link Dockable} for the specified {@link DockableState}.
	 */
	private Dockable buildDockable(
			final DockableState dockableState
	) {
		final DockBuilding dockBuilding = bento.dockBuilding();

		final Dockable dockable = dockBuilding.dockable(dockableState.getIdentifier());
		dockableState.getDockableNode().ifPresent(dockable::setNode);
		dockableState.getTitle().ifPresent(dockable::setTitle);
		dockableState.getTooltipText().ifPresent(tooltipText -> dockable.setTooltip(new Tooltip(tooltipText)));
		dockableState.getDockableIconFactory().ifPresent(dockable::setIconFactory);
		dockableState.getDockableMenuFactory().ifPresent(dockable::setContextMenuFactory);
		return dockable;
	}

	/**
	 * Called when a {@link DockEvent.DockableClosing} occurs.
	 *
	 * @param closingEvent the {@link Event} that occurred when the
	 * {@link DockContainerLeaf} closed a {@link Dockable} item.
	 */
	private void handleDockableClosing(DockEvent.DockableClosing closingEvent) {
		final Dockable dockable = closingEvent.dockable();
		if (!dockable.getTitle().startsWith("Class "))
			return;

		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText(null);
		alert.setContentText("Save changes to [" + dockable.getTitle() + "] before closing?");
		alert.getButtonTypes().setAll(
				ButtonType.YES,
				ButtonType.NO,
				ButtonType.CANCEL
		);

		final ButtonType result = alert.showAndWait()
				.orElse(ButtonType.CANCEL);

		if (result.equals(ButtonType.YES)) {
			// simulate saving application (not docking layout) state
			logger.debug("Saving {}...", dockable.getTitle());

		} else if (result.equals(ButtonType.NO)) {
			// nothing to do - just close
			logger.debug("Closing {} without saving...", dockable.getTitle());
		} else if (result.equals(ButtonType.CANCEL)) {
			// prevent closing
			closingEvent.cancel();
		}
	}

	/**
	 * Optionally adds the {@code Dockable} with the provided {@code dockableId}
	 * to the {@code DockContainer}. Logs a warning message when the
	 * {@code Dockable} cannot be resolved using the {@code dockableId}.
	 *
	 * @param dockableProperties the identifier for the {@code Dockable} to add.
	 * @param container the {@code DockContainer} to which the {@code Dockable}
	 * should be added.
	 */
	private void addDockable(
			final DockableProperties dockableProperties,
			final DockableStateProvider dockableStateProvider,
			final DockContainer container
	) {
		dockableStateProvider.resolveDockableState(dockableProperties.getIdentifier())
				.ifPresentOrElse(
						dockableState ->
								// Our application isn't doing anything with the
								// reconstructed Dockable. Just add it to the
								// container.
								container.addDockable(buildDockable(dockableState)),
						() -> logger.warn("Could not add dockable {}.", dockableProperties)
				);
	}

	/**
	 * {@return the {@link LayoutSaver} for this application's layout, or
	 * {@code null} when one cannot be created.}
	 *
	 * <p>The saver returned from a {@link DockingLayoutPersistenceProvider}
	 * already has auto-save running, so this is called once, while the
	 * application is starting, rather than where the layout is saved.</p>
	 */
	private @Nullable LayoutSaver createLayoutSaver() {
		try {
			return persistenceProvider.getLayoutSaver(
					DEFAULT_LAYOUT_IDENTIFIER,
					bentoProvider
			);
		} catch (final BentoStateException e) {
			logger.warn("Could not create the docking layout saver.", e);
			return null;
		}
	}

	/**
	 * {code EventHandler<WindowEvent>} implementation that saves the docking
	 * layout and then releases the saver; it does <b><i><u>not</u></i></b> save
	 * the layout of the main Stage, non-docking components, or other application
	 * state.
	 *
	 * <p>The saver is closed here rather than from {@code stop()} because this
	 * runs while the windows still exist, and closing is what removes the saver's
	 * listener from each {@code Bento} and stops its auto-save. Saving explicitly
	 * first is deliberate: closing saves only when a dock event has been received
	 * since the last save.</p>
	 *
	 * @param windowEvent unused.
	 */
	private void saveDockingLayout(final WindowEvent windowEvent) {
		final LayoutSaver saver = layoutSaver;

		try (saver) {
			if (saver == null) {
				return;
			}
			saver.saveLayout();
		} catch (final BentoStateException e) {
			logger.warn("Could not save the docking layout.", e);
		}
	}

	/**
	 * @return if a prior {@link DockingLayout} has been saved, restores and
	 * returns it. Otherwise, returns the default {@link DockingLayout}.
	 *
	 * @see #getDefaultDockingLayout()
	 */
	private DockingLayout getDockingLayout() {

		// The restorer owns the LayoutStorage it was given and closes it, so it is
		// closed here rather than abandoned. The layout it returns is already built,
		// so closing the storage afterwards costs nothing.
		try (final LayoutRestorer layoutRestorer =
					 persistenceProvider.getLayoutRestorer(
							 DEFAULT_LAYOUT_IDENTIFIER,
							 bentoProvider,
							 dockableStateProvider,
							 stageIconImageProvider,
							 dockContainerLeafMenuFactoryProvider
					 )) {

			return layoutRestorer.restoreLayout(
					this::getDefaultDockingLayout
			);
		} catch (BentoStateException e) {
			logger.warn("Could not create the docking layout restorer.", e);
			return getDefaultDockingLayout();
		}
	}

	/**
	 * Applies all {@link BentoLayout} found in the {@link DockingLayout}.
	 *
	 * @param dockingLayout the {@link DockingLayout} to be applied.
	 *
	 * @return {@code true} when at least one {@link BentoLayout} was applied;
	 * otherwise, {@code false}, which means no {@link Scene} was set and the
	 * caller has to apply a layout that can be.
	 */
	private boolean applyDockingLayout(
			final DockingLayout dockingLayout
	) {
		boolean isApplied = false;

		for (final BentoLayout bentoLayout :
				dockingLayout.getBentoLayouts()) {
			if (bentoLayout.matchesIdentity(bento)) {
				isApplied |= applyBentoLayout(bentoLayout);
			} else {
				logger.warn(
						"Unknown BentoLayout identifier: {}",
						bentoLayout.getIdentifier()
				);
			}
		}

		return isApplied;
	}

	/**
	 * Builds and returns the {@link DockingLayout} for {@link #bento} and
	 * {@link #defaultRootBranches}.
	 *
	 * @return the {@link DockingLayout} for {@link #bento} and
	 * {@link #defaultRootBranches}.
	 */
	private DockingLayout getDefaultDockingLayout() {

		DockingLayoutBuilder dockingLayoutBuilder =
				new DockingLayoutBuilder();

		BentoLayoutBuilder bentoLayoutBuilder = new BentoLayoutBuilder(
				bento.getIdentifier()
		);
		for (final DockContainerRootBranch rootBranch : defaultRootBranches) {
			bentoLayoutBuilder.addRootBranch(rootBranch);
		}
		dockingLayoutBuilder.addBentoLayout(bentoLayoutBuilder.build());

		return dockingLayoutBuilder.build();
	}

	/**
	 * Applies the {@link BentoLayout} to docking components.
	 *
	 * @param bentoLayout the layout to be applied.
	 *
	 * @return {@code true} when the layout was applied to the {@link Stage};
	 * otherwise, {@code false}.
	 */
	private boolean applyBentoLayout(final BentoLayout bentoLayout) {
		final List<DockContainerRootBranch> bentoRootBranches =
				bentoLayout.getRootBranches();

		if (bentoRootBranches.size() != 1) {
			// This stage only has one root branch
			logger.error(
					"The stage should have one root branch but {} " +
							"were found.",
					bentoRootBranches.size()
			);
			return false;
		}

		if (stage == null) {
			// The primary stage should have been set when the application was started
			logger.error("The stage cannot be null.");
			return false;
		}

		if (!bentoLayout.matchesIdentity(bento)) {
			// A DockingLayout can have multiple BentoLayout; make sure we're
			// applying the right one
			logger.warn(
					"Cannot apply BentoLayout {} to {}.",
					bentoLayout.getIdentifier(),
					bento.getIdentifier()
			);
			return false;
		}

		// Apply the root branch of the BentoLayout
		final Scene scene =
				new Scene(bentoRootBranches.getFirst());
		scene.getStylesheets().add("/bento.css");
		stage.setScene(scene);
		stage.show();

		// Show the DragDropStages that were showing when the layout was saved.
		// Showing all of them unconditionally is what made the persisted
		// isShowing flag pointless: a stage the user had closed came back open.
		// Only reached once the main layout is up: floating windows from a layout
		// whose root branch could not be applied would be the only thing on screen.
		for (final DragDropStage dragDropStage :
				bentoLayout.getDragDropStages()) {
			if (bentoLayout.wasShowing(dragDropStage)) {
				dragDropStage.show();
			}
		}

		return true;
	}
}
