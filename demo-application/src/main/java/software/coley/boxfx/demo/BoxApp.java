package software.coley.boxfx.demo;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.*;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.util.ServiceLoader;

import static software.coley.boxfx.demo.provider.BoxAppDockableProvider.*;

/**
 * JavaFX application that demonstrates using the BentoFX framework.
 */
public class BoxApp extends Application {

    private static final Logger logger =
            LoggerFactory.getLogger(BoxApp.class);

    private DockBuilding builder;
    private LayoutStorage layoutStorage;
    private DockableProvider dockableProvider;
    private ImageProvider imageProvider;
    private DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider;
    private DockableMenuFactoryProvider dockableMenuFactoryProvider;
    private LayoutSaver layoutSaver;
    private LayoutRestorer layoutRestorer;

    /**
     * Uses {@link ServiceLoader} and Service Provider Interfaces to acquire
     * injected dependencies before starting the JavaFX application.
     */
    @Override
    public void init() {

        final Bento bento = new Bento();
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

        builder = bento.dockBuilding();

        // Use the ServiceLoader to inject Service Provider implementations for
        // storing, encoding, and decoding Bento layouts.

        // LayoutCodecProvider
        final Iterable<LayoutCodecProvider> codecProviders =
                ServiceLoader.load(LayoutCodecProvider.class);
        final LayoutCodecProvider codecProvider =
                codecProviders.iterator().next();
        final LayoutCodec layoutCodec = codecProvider.createLayoutCodec();

        // LayoutStorageProvider
        final Iterable<LayoutStorageProvider> storageProviders =
                ServiceLoader.load(LayoutStorageProvider.class);
        final LayoutStorageProvider storageProvider =
                storageProviders.iterator().next();
        layoutStorage =
                storageProvider.createLayoutStorage(
                        layoutCodec.getIdentifier()
                );

        // DockableProvider
        final Iterable<DockableProvider> dockableProviders =
                ServiceLoader.load(DockableProvider.class);

        dockableProvider = dockableProviders.iterator().next();
        dockableProvider.init(builder, dockableMenuFactoryProvider);

        // ImageProvider
        final Iterable<ImageProvider> imageProviders =
                ServiceLoader.load(ImageProvider.class);

        imageProvider = imageProviders.iterator().next();

        // DockContainerLeafMenuFactoryProvider
        final Iterable<DockContainerLeafMenuFactoryProvider> dockContainerLeafMenuFactoryProviders =
                ServiceLoader.load(DockContainerLeafMenuFactoryProvider.class);

        dockContainerLeafMenuFactoryProvider =
                dockContainerLeafMenuFactoryProviders.iterator().next();

        // DockableMenuFactoryProvider
        final Iterable<DockableMenuFactoryProvider> dockableMenuFactoryProviders =
                ServiceLoader.load(DockableMenuFactoryProvider.class);

        dockableMenuFactoryProvider =
                dockableMenuFactoryProviders.iterator().next();

        // LayoutPersistenceProvider
        final Iterable<LayoutPersistenceProvider> persistenceProviders =
                ServiceLoader.load(LayoutPersistenceProvider.class);
        final LayoutPersistenceProvider persistenceProvider =
                persistenceProviders.iterator().next();

        layoutSaver = persistenceProvider.createLayoutSaver(
                bento,
                layoutStorage,
                layoutCodec
        );

        layoutRestorer = persistenceProvider.createLayoutRestorer(
                bento,
                layoutStorage,
                layoutCodec,
                dockableProvider,
                imageProvider,
                dockContainerLeafMenuFactoryProvider
        );
    }

    @Override
    public void start(Stage stage) {

        stage.setWidth(1000);
        stage.setHeight(700);
        stage.getIcons().addAll(imageProvider.getDefaultStageIcons());
        stage.setTitle("BentoFX Demo");

        DockContainerRootBranch branchRoot;

        // If a prior layout has been saved, restore the BentoFX layout from the
        // persisted state. Otherwise, use the default layout.
        if (layoutStorage.exists()) {
            try {
                branchRoot = layoutRestorer.restoreLayout(stage);

            } catch (final BentoStateException e) {

                logger.warn(
                        "Could not restore the saved layout; using the " +
                                "default layout instead.",
                        e
                );

                branchRoot =
                        constructDefaultDockContainerRootBranch();
            }
        } else {

            branchRoot =
                    constructDefaultDockContainerRootBranch();
        }

        Scene scene = new Scene(branchRoot);
        scene.getStylesheets().add("/bento.css");
        stage.setScene(scene);
        stage.setOnHidden(e -> System.exit(0));
        stage.setOnCloseRequest(event -> {

            try {

                layoutSaver.saveLayout();
            } catch (BentoStateException e) {

                logger.error("Could not save the Bento layout.", e);
            }
        });
        stage.show();
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

            try {

                layoutSaver.saveLayout();
            } catch (BentoStateException e) {

                logger.error("Could not save the Bento layout.", e);
            }
        } else if (result.equals(ButtonType.NO)) {

            // nothing to do - just close
            logger.debug("Closing {} without saving...", dockable.getTitle());
        } else if (result.equals(ButtonType.CANCEL)) {

            // prevent closing
            closingEvent.cancel();
        }
    }

    private DockContainerRootBranch constructDefaultDockContainerRootBranch() {

        DockContainerRootBranch branchRoot = builder.root("root");
        DockContainerBranch branchWorkspace = builder.branch("workspace");
        DockContainerLeaf leafWorkspaceTools = builder.leaf("workspace-tools");
        DockContainerLeaf leafWorkspaceHeaders = builder.leaf("workspace-headers");
        DockContainerLeaf leafTools = builder.leaf("misc-tools");

        branchWorkspace.setPruneWhenEmpty(false);
        leafWorkspaceTools.setPruneWhenEmpty(false);
        leafTools.setPruneWhenEmpty(false);
        leafTools.setPruneWhenEmpty(false);

        // Add dummy menus to each.
        leafTools.setMenuFactory(
                dockContainerLeafMenuFactoryProvider.createDockContainerLeafMenuFactory(
                        leafTools
                )
        );
        leafWorkspaceHeaders.setMenuFactory(
                dockContainerLeafMenuFactoryProvider.createDockContainerLeafMenuFactory(
                        leafWorkspaceHeaders
                )
        );
        leafWorkspaceTools.setMenuFactory(
                dockContainerLeafMenuFactoryProvider.createDockContainerLeafMenuFactory(
                        leafWorkspaceTools
                )
        );

        // These leaves shouldn't auto-expand. They are intended to be a set size.
        DockContainerBranch.setResizableWithParent(leafTools, false);
        DockContainerBranch.setResizableWithParent(leafWorkspaceTools, false);

        // Root: Workspace on top, tools on bottom
        // Workspace: Explorer on left, primary editor tabs on right
        branchRoot.setOrientation(Orientation.VERTICAL);
        branchWorkspace.setOrientation(Orientation.HORIZONTAL);
        branchRoot.addContainers(branchWorkspace, leafTools);
        branchWorkspace.addContainers(leafWorkspaceTools, leafWorkspaceHeaders);

        // Changing tool header sides to be aligned with application's far edges (to facilitate better collapsing UX)
        leafWorkspaceTools.setSide(Side.LEFT);
        leafTools.setSide(Side.BOTTOM);

        // TODO BENTO-13 Persist/restore canSplit
        // Tools shouldn't allow splitting (mirroring IntelliJ behavior)
        leafWorkspaceTools.setCanSplit(false);
        leafTools.setCanSplit(false);

        // Primary editor space should not prune when empty
        leafWorkspaceHeaders.setPruneWhenEmpty(false);

        // TODO BENTO-13: Persist/Restore containerSizePx
        // Set intended sizes for tools (leaf does not need to be a direct child, just some level down in the chain)
        branchRoot.setContainerSizePx(leafTools, 200);
        branchRoot.setContainerSizePx(leafWorkspaceTools, 300);

        // TODO BENTO-13: Persist/Restore containerCollapsed
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
     * @param dockableId the identifier for the {@code Dockable} to add.
     * @param container the {@code DockContainer} to which the {@code Dockable}
     *                  should be added.
     */
    private void addDockable(
            final @NotNull String dockableId,
            final @NotNull DockContainer container
    ) {
        dockableProvider.resolveDockable(dockableId)
                .ifPresentOrElse(
                        container::addDockable,
                        () -> logger.warn("Could not add dockable {}.", dockableId)
                );

    }
}
