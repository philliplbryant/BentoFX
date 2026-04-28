package software.coley.boxfx.demo.persistence.ui;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
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
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.impl.BentoLayout;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.boxfx.demo.persistence.provider.DockableProperties;

import java.util.ArrayList;
import java.util.List;

import static software.coley.boxfx.demo.persistence.provider.BoxAppDockableStateProvider.*;
import static software.coley.boxfx.demo.persistence.ui.DockableUtils.createDockable;

/**
 * {@link Stage} implementation derived from the original {@code BoxApp} class
 * in the basic demo, modified to demonstrate using the persistence framework to
 * save and restore docking layouts.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
public class MainStage extends Stage {

    private static final Logger logger =
            LoggerFactory.getLogger(MainStage.class);

    private final Bento bento = new Bento("box-app-bento");
    private final List<DockContainerRootBranch> rootBranches =
            new ArrayList<>();

    public MainStage(
            final DefaultBentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
            final StageIconImageProvider stageIconImageProvider,
            final DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider,
            final Runnable onCloseRequestRunnable
    ) {
        initBento(bentoProvider);
        initUi(
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider,
                onCloseRequestRunnable
        );
    }

    public Bento getBento() {
        return bento;
    }

    public List<DockContainerRootBranch> getRootBranches() {
        return rootBranches;
    }

    public void restoreLayout(final BentoLayout bentoLayout) {

        // This stage should only have one root branch
        final List<DockContainerRootBranch> bentoRootBranches =
                bentoLayout.getRootBranches();

        if (bentoRootBranches.size() != 1) {
            logger.error(
                    "The MainStage should have one root branch but {} " +
                            "were found.",
                    bentoRootBranches.size()
            );
        } else {
            final Scene scene =
                    new Scene(bentoRootBranches.getFirst());
            scene.getStylesheets().add("/bento.css");
            setScene(scene);
            show();
        }

        for (final DragDropStage dragDropStage :
                bentoLayout.getDragDropStages()) {
            dragDropStage.show();
        }
    }

    private void initBento(final DefaultBentoProvider bentoProvider) {
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

        bento.stageBuilding().setApplyMousePosition(true);
        bento.stageBuilding().setApplySourceAsOwner(false);

        bentoProvider.addBento(bento);
    }

    private void initUi(
            final DockableStateProvider dockableStateProvider,
            final StageIconImageProvider stageIconImageProvider,
            final DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider,
            final Runnable onCloseRequestRunnable
    ) {

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

        // Changing tool header sides to be aligned with application's far edges
        // (to facilitate better collapsing UX)
        leafWorkspaceTools.setSide(Side.LEFT);
        leafTools.setSide(Side.BOTTOM);

        // Tools shouldn't allow splitting (mirroring IntelliJ behavior)
        leafWorkspaceTools.setCanSplit(false);
        leafTools.setCanSplit(false);

        // Primary editor space should not prune when empty
        leafWorkspaceHeaders.setPruneWhenEmpty(false);

        // Set intended sizes for tools (leaf does not need to be a direct
        // child, just some level down in the chain)
        branchRoot.setContainerSizePx(leafTools, 200);
        branchRoot.setContainerSizePx(leafWorkspaceTools, 300);

        // Make the bottom collapsed by default
        branchRoot.setContainerCollapsed(leafTools, true);

        // Add dockables to leafWorkspaceTools
        addDockable(bento, workspaceDockableProperties, dockableStateProvider, leafWorkspaceTools);
        addDockable(bento, bookmarksDockableProperties, dockableStateProvider, leafWorkspaceTools);
        addDockable(bento, modificationsDockableProperties, dockableStateProvider, leafWorkspaceTools);

        // Add dockables to leafTools
        addDockable(bento, loggingDockableProperties, dockableStateProvider, leafTools);
        addDockable(bento, terminalDockableProperties, dockableStateProvider, leafTools);
        addDockable(bento, problemsDockableProperties, dockableStateProvider, leafTools);

        // Add dockables to leafWorkspaceHeaders
        addDockable(bento, class1DockableProperties, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, class2DockableProperties, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, class3DockableProperties, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, class4DockableProperties, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, class5DockableProperties, dockableStateProvider, leafWorkspaceHeaders);

        rootBranches.add(branchRoot);

        setTitle("BentoFX Demo");
        setWidth(1000);
        setHeight(700);
        getIcons().addAll(
                stageIconImageProvider.getStageIcons()
        );
        centerOnScreen();
        // We need to save the docking layout on close request
        // because the stage is (and all other windows are)
        // no longer available after they are closed and, as such,
        // will not be discoverable when saving the docking layout.
        setOnCloseRequest(event ->
                onCloseRequestRunnable.run()
        );
        setOnHidden(e -> System.exit(0));
    }

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
     * @param container  the {@code DockContainer} to which the {@code Dockable}
     *                   should be added.
     */
    private void addDockable(
            final Bento bento,
            final DockableProperties dockableProperties,
            final DockableStateProvider dockableStateProvider,
            final DockContainer container
    ) {
        dockableStateProvider.resolveDockableState(dockableProperties.identifier())
                .ifPresentOrElse(
                        dockableState ->
                                // Our application isn't doing anything with the
                                // reconstructed Dockable. Just add it to the
                                // container.
                                container.addDockable(
                                        createDockable(bento, dockableState)
                                ),
                        () ->
                                logger.warn(
                                        "Could not add dockable {}.",
                                        dockableProperties
                                )
                );
    }
}
