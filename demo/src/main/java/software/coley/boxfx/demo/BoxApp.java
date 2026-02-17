package software.coley.boxfx.demo;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.event.DockEvent;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.DockableState;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.impl.provider.BentoLayoutPersistenceProvider;
import software.coley.boxfx.demo.provider.BoxAppDockContainerLeafMenuFactoryProvider;
import software.coley.boxfx.demo.provider.BoxAppDockableMenuFactory;
import software.coley.boxfx.demo.provider.BoxAppDockableStateProvider;
import software.coley.boxfx.demo.provider.BoxAppStageIconImageProvider;

import java.util.ServiceLoader;

import static software.coley.boxfx.demo.provider.BoxAppDockableStateProvider.*;

/**
 * JavaFX application that demonstrates using the BentoFX docking and docking
 * persistence frameworks.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
public class BoxApp extends Application {

    private static final Logger logger =
            LoggerFactory.getLogger(BoxApp.class);

    private static final String DEFAULT_LAYOUT_IDENTIFIER = "recent";

    private final LayoutPersistenceProvider persistenceProvider =
            new BentoLayoutPersistenceProvider();

    private final DockableStateProvider dockableStateProvider =
            new BoxAppDockableStateProvider(
                    new BoxAppDockableMenuFactory()
            );

    private final StageIconImageProvider stageIconImageProvider =
            new BoxAppStageIconImageProvider();

    private final DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider =
            new BoxAppDockContainerLeafMenuFactoryProvider();

    private Bento bento;

    /**
     * Uses {@link ServiceLoader} and Service Provider Interfaces to acquire
     * injected dependencies before starting the JavaFX application.
     */
    @Override
    public void init() {

        bento = new Bento(getClass().getSimpleName());
        initializeBento(bento);
    }

    @Override
    public void start(Stage stage) {

        stage.setWidth(1000);
        stage.setHeight(700);
        stage.getIcons().addAll(stageIconImageProvider.getStageIcons());
        stage.setTitle("BentoFX Demo");

        DockContainerRootBranch branchRoot = restoreBranch(stage);

        Scene scene = new Scene(branchRoot);
        scene.getStylesheets().add("/bento.css");
        stage.setScene(scene);
        stage.setOnHidden(e -> System.exit(0));
        // We need to save the docking layout on close request because the
        // primary stage is (and all other windows are) no longer available
        // after closed and, as such, will not be included when saving the
        // docking layout.
        stage.setOnCloseRequest(event -> saveDockingLayout());

        // We don't need to wait for dockables to be initialized so we can show
        // all of our stages now.
        // TODO BENTO-13: Use the Bento to show all stages
        stage.show();
    }

    private void initializeBento(final @NotNull Bento bento) {

        bento.placeholderBuilding().setDockablePlaceholderFactory(dockable ->
                new Label("Empty Dockable")
        );
        bento.placeholderBuilding().setContainerPlaceholderFactory(container ->
                new Label("Empty Container")
        );
        bento.events().addEventListener((DockEvent event) -> {
            if (event instanceof DockEvent.DockableClosing closingEvent)
                handleDockableClosing(closingEvent);
        });
    }

    private DockContainerRootBranch restoreBranch(final Stage stage) {

        DockContainerRootBranch branchRoot;

        final LayoutRestorer layoutRestorer =
                persistenceProvider.getLayoutRestorer(
                        bento,
                        DEFAULT_LAYOUT_IDENTIFIER,
                        dockableStateProvider,
                        stageIconImageProvider,
                        dockContainerLeafMenuFactoryProvider
                );

        // If a prior docking layout has been saved, restore it from the
        // persisted state. Otherwise, use the default layout.
        if (layoutRestorer.doesLayoutExist()) {

            branchRoot = layoutRestorer.restoreLayout(
                    stage,
                    this::getDefaultLayout
            );

            // Update the new Bento
            bento = branchRoot.getBento();
            // TODO BENTO-13: Re-initialize the Bento when restoring it
            initializeBento(bento);

        } else {

            branchRoot = getDefaultLayout();
        }

        return branchRoot;
    }

    private void saveDockingLayout() {

        try {

            final LayoutSaver layoutSaver =
                    persistenceProvider.getLayoutSaver(
                            bento,
                            DEFAULT_LAYOUT_IDENTIFIER
                    );

            layoutSaver.saveLayout();

        } catch (BentoStateException e) {

            logger.warn("Could not save the docking layout.", e);
        }
    }

    private void handleDockableClosing(@NotNull DockEvent.DockableClosing closingEvent) {
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

    private DockContainerRootBranch getDefaultLayout() {

        final DockBuilding dockBuilding = bento.dockBuilding();

        DockContainerRootBranch branchRoot = dockBuilding.root("root");
        DockContainerBranch branchWorkspace = dockBuilding.branch("workspace");
        DockContainerLeaf leafWorkspaceTools = dockBuilding.leaf("workspace-tools");
        DockContainerLeaf leafWorkspaceHeaders = dockBuilding.leaf("workspace-headers");
        DockContainerLeaf leafTools = dockBuilding.leaf("misc-tools");

        branchWorkspace.setPruneWhenEmpty(false);
        leafWorkspaceTools.setPruneWhenEmpty(false);
        leafTools.setPruneWhenEmpty(false);
        leafTools.setPruneWhenEmpty(false);

        // Add dummy menus to each.
        dockContainerLeafMenuFactoryProvider.createDockContainerLeafMenuFactory(
                leafTools.getIdentifier()
        ).ifPresent(leafTools::setMenuFactory);

        dockContainerLeafMenuFactoryProvider.createDockContainerLeafMenuFactory(
                leafWorkspaceHeaders.getIdentifier()
        ).ifPresent(leafWorkspaceHeaders::setMenuFactory);

        dockContainerLeafMenuFactoryProvider.createDockContainerLeafMenuFactory(
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

        // Add dockables to leafWorkspaceTools
        addDockable(WORKSPACE_DOCKABLE_ID, leafWorkspaceTools);
        addDockable(BOOKMARKS_DOCKABLE_ID, leafWorkspaceTools);
        addDockable(MODIFICATIONS_DOCKABLE_ID, leafWorkspaceTools);

        // Add dockables to leafTools
        addDockable(LOGGING_DOCKABLE_ID, leafTools);
        addDockable(TERMINAL_DOCKABLE_ID, leafTools);
        addDockable(PROBLEMS_DOCKABLE_ID, leafTools);

        // Add dockables to leafWorkspaceHeaders
        addDockable(CLASS_1_DOCKABLE_ID, leafWorkspaceHeaders);
        addDockable(CLASS_2_DOCKABLE_ID, leafWorkspaceHeaders);
        addDockable(CLASS_3_DOCKABLE_ID, leafWorkspaceHeaders);
        addDockable(CLASS_4_DOCKABLE_ID, leafWorkspaceHeaders);
        addDockable(CLASS_5_DOCKABLE_ID, leafWorkspaceHeaders);

        return branchRoot;
    }

    /**
     * Optionally adds the {@code Dockable} with the provided {@code dockableId}
     * to the {@code DockContainer}. Logs a warning message when the
     * {@code Dockable} cannot be resolved using the {@code dockableId}.
     *
     * @param dockableId the identifier for the {@code Dockable} to add.
     * @param container  the {@code DockContainer} to which the {@code Dockable}
     *                   should be added.
     */
    private void addDockable(
            final @NotNull String dockableId,
            final @NotNull DockContainer container
    ) {
        dockableStateProvider.resolveDockableState(dockableId)
                .ifPresentOrElse(
                        dockableState ->
                                // Our application isn't doing anything with the
                                // reconstructed Dockable. Just add it to the
                                // container.
                                container.addDockable(
                                        createDockable(dockableState)
                                ),
                        () ->
                                logger.warn(
                                        "Could not add dockable {}.",
                                        dockableId
                                )
                );
    }

    private @NotNull Dockable createDockable(
            @NotNull DockableState dockableState
    ) {
        final DockBuilding dockBuilding = bento.dockBuilding();

        final Dockable dockable =
                dockBuilding.dockable(dockableState.getIdentifier());

        dockableState.getDockableNode().ifPresent(
                dockable::setNode
        );

        dockableState.getTitle().ifPresent(
                dockable::setTitle
        );

        dockableState.getDockableIconFactory().ifPresent(
                dockable::setIconFactory
        );

        dockableState.getDockableMenuFactory().ifPresent(
                dockable::setContextMenuFactory
        );

        return dockable;
    }
}
