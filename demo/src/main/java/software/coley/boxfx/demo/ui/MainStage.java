package software.coley.boxfx.demo.ui;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
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
import software.coley.bentofx.persistence.api.IdentifiableStageLayout;
import software.coley.bentofx.persistence.api.PersistableStage;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.boxfx.demo.provider.BoxAppBentoProvider;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.StageUtils.getStageStateBuilder;
import static software.coley.boxfx.demo.provider.BoxAppDockableStateProvider.*;

public class MainStage extends PersistableStage {

    private static final Logger logger =
            LoggerFactory.getLogger(MainStage.class);

    private final @NotNull Bento bento = new Bento("box-app-bento");
    private final @NotNull List<DockContainerRootBranch> rootBranches =
            new ArrayList<>();

    public MainStage(
            final @NotNull BoxAppBentoProvider bentoProvider,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider,
            final @NotNull DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider,
            final @NotNull Runnable onCloseRequestRunnable
    ) {
        super("main-stage");
        initBento(bentoProvider);
        initUi(
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider,
                onCloseRequestRunnable
        );
    }

    @Override
    public @NotNull Bento getBento() {
        return bento;
    }

    @Override
    public @NotNull IdentifiableStageLayout getLayout() {
        return new IdentifiableStageLayout(
                getStageStateBuilder(this).build(),
                rootBranches
        );
    }

    private void initBento(final @NotNull BoxAppBentoProvider bentoProvider) {
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

        bentoProvider.addBento(bento);
    }

    private void initUi(
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider,
            final @NotNull DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider,
            final @NotNull Runnable onCloseRequestRunnable
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
        addDockable(bento, WORKSPACE_DOCKABLE_ID, dockableStateProvider, leafWorkspaceTools);
        addDockable(bento, BOOKMARKS_DOCKABLE_ID, dockableStateProvider, leafWorkspaceTools);
        addDockable(bento, MODIFICATIONS_DOCKABLE_ID, dockableStateProvider, leafWorkspaceTools);

        // Add dockables to leafTools
        addDockable(bento, LOGGING_DOCKABLE_ID, dockableStateProvider, leafTools);
        addDockable(bento, TERMINAL_DOCKABLE_ID, dockableStateProvider, leafTools);
        addDockable(bento, PROBLEMS_DOCKABLE_ID, dockableStateProvider, leafTools);

        // Add dockables to leafWorkspaceHeaders
        addDockable(bento, CLASS_1_DOCKABLE_ID, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, CLASS_2_DOCKABLE_ID, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, CLASS_3_DOCKABLE_ID, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, CLASS_4_DOCKABLE_ID, dockableStateProvider, leafWorkspaceHeaders);
        addDockable(bento, CLASS_5_DOCKABLE_ID, dockableStateProvider, leafWorkspaceHeaders);

        rootBranches.add(branchRoot);

        setTitle("BentoFX Demo");
        setWidth(1000);
        setHeight(700);
        getIcons().addAll(
                stageIconImageProvider.getStageIcons()
        );
        // We need to save the docking layout on close request
        // because the stage is (and all other windows are)
        // no longer available after they are closed and, as such,
        // will not be discoverable when saving the docking layout.
        setOnCloseRequest(event ->
                onCloseRequestRunnable.run()
        );
        setOnHidden(e -> System.exit(0));
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
            final @NotNull Bento bento,
            final @NotNull String dockableId,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull DockContainer container
    ) {
        dockableStateProvider.resolveDockableState(dockableId)
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
                                        dockableId
                                )
                );
    }
}
