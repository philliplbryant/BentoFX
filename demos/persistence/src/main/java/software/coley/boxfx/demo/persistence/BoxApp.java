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
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.state.DockableState;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.bentofx.persistence.impl.provider.DockingLayoutPersistenceProvider;
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
	 * Collect the {@link DockContainerRootBranch} so they can be persisted.
	 */
	private final List<DockContainerRootBranch> rootBranches =
			new ArrayList<>();

	private final LayoutPersistenceProvider persistenceProvider =
			new DockingLayoutPersistenceProvider();

	private final DockableStateProvider dockableStateProvider =
			new BoxAppDockableStateProvider(
					new BoxAppDockableMenuFactoryProvider()
			);

	private final StageIconImageProvider stageIconImageProvider =
			new BoxAppStageIconImageProvider();

	private final DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider =
			new BoxAppDockContainerLeafMenuFactoryProvider();

	private final DefaultBentoProvider bentoProvider =
			new DefaultBentoProvider();

	private @Nullable Stage stage;

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

		// Initialize the BentoProvider
		bentoProvider.addBento(bento);

		final DockBuilding builder = bento.dockBuilding();
		final DockContainerRootBranch branchRoot = builder.root("root");
		final DockContainerBranch branchWorkspace = builder.branch("workspace");
		final DockContainerLeaf leafWorkspaceTools = builder.leaf("workspace-tools");
		final DockContainerLeaf leafWorkspaceHeaders = builder.leaf("workspace-headers");
		final DockContainerLeaf leafTools = builder.leaf("misc-tools");

		branchWorkspace.setPruneWhenEmpty(false);
		leafWorkspaceTools.setPruneWhenEmpty(false);
		leafTools.setPruneWhenEmpty(false);
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

		rootBranches.add(branchRoot);

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
		applyDockingLayout(dockingLayout);
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
	 * {code EventHandler<WindowEvent>} implementation that saves the docking
	 * layout; it does <b><i><u>not</u></i></b> save the layout of the main
	 * Stage, non-docking components, or other application state.
	 *
	 * @param windowEvent unused.
	 */
	private void saveDockingLayout(final WindowEvent windowEvent) {
		try {
			final LayoutSaver layoutSaver =
					persistenceProvider.getLayoutSaver(
							bentoProvider,
							DEFAULT_LAYOUT_IDENTIFIER
					);

			layoutSaver.saveLayout();
		} catch (BentoStateException e) {
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

		final LayoutRestorer layoutRestorer =
				persistenceProvider.getLayoutRestorer(
						bentoProvider,
						DEFAULT_LAYOUT_IDENTIFIER,
						dockableStateProvider,
						stageIconImageProvider,
						dockContainerLeafMenuFactoryProvider
				);

		return layoutRestorer.restoreLayout(
				this::getDefaultDockingLayout
		);
	}

	/**
	 * Applies all {@link BentoLayout} found in the {@link DockingLayout}.
	 *
	 * @param dockingLayout the {@link DockingLayout} to be applied.
	 */
	private void applyDockingLayout(
			final DockingLayout dockingLayout
	) {
		for (final BentoLayout bentoLayout :
				dockingLayout.getBentoLayouts()) {
			if (bentoLayout.matchesIdentity(bento)) {
				applyBentoLayout(bentoLayout);
			} else {
				logger.warn(
						"Unknown BentoLayout identifier: {}",
						bentoLayout.getIdentifier()
				);
			}
		}
	}

	/**
	 * Builds and returns the {@link DockingLayout} for {@link #bento} and
	 * {@link #rootBranches}.
	 *
	 * @return the {@link DockingLayout} for {@link #bento} and
	 * {@link #rootBranches}.
	 */
	private DockingLayout getDefaultDockingLayout() {

		DockingLayoutBuilder dockingLayoutBuilder =
				new DockingLayoutBuilder();

		BentoLayoutBuilder bentoLayoutBuilder = new BentoLayoutBuilder(
				bento.getIdentifier()
		);
		for (final DockContainerRootBranch rootBranch : rootBranches) {
			bentoLayoutBuilder.addRootBranch(rootBranch);
		}
		dockingLayoutBuilder.addBentoLayout(bentoLayoutBuilder.build());

		return dockingLayoutBuilder.build();
	}

	/**
	 * Applies the {@link BentoLayout} to docking components.
	 *
	 * @param bentoLayout the layout to be applied.
	 */
	public void applyBentoLayout(final BentoLayout bentoLayout) {
		final List<DockContainerRootBranch> bentoRootBranches =
				bentoLayout.getRootBranches();

		if (bentoRootBranches.size() != 1) {
			// This stage only has one root branch
			logger.error(
					"The stage should have one root branch but {} " +
							"were found.",
					bentoRootBranches.size()
			);
		} else if (stage == null) {
			// The primary stage should have been set when the application was started
			logger.error("The stage cannot be null.");
		} else if (!bentoLayout.matchesIdentity(bento)) {
			// A DockingLayout can have multiple BentoLayout; make sure we're
			// applying the right one
			logger.warn(
					"Cannot apply BentoLayout {} to {}.",
					bentoLayout.getIdentifier(),
					bento.getIdentifier()
			);
		} else {
			// Apply the root branch of the BentoLayout
			final Scene scene =
					new Scene(bentoRootBranches.getFirst());
			scene.getStylesheets().add("/bento.css");
			stage.setScene(scene);
			stage.show();
		}

		// Show any DragDropStages
		for (final DragDropStage dragDropStage :
				bentoLayout.getDragDropStages()) {
			dragDropStage.show();
		}
	}
}
