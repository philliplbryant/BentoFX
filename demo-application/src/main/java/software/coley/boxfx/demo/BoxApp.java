package software.coley.boxfx.demo;

import jakarta.annotation.Nonnull;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.event.DockEvent;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.persistence.api.DockableProvider;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;

import java.util.ServiceLoader;

public class BoxApp extends Application {

    private DockBuilding builder;
    private DockableProvider dockableProvider;
    private LayoutSaver layoutSaver;
    private LayoutRestorer layoutRestorer;

    /**
     * Acquires injected application dependencies before starting the JavaFX
     * application.
     */
    @Override
    public void init() {

        Bento bento = new Bento();
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
        // encoding/decoding and storing Bento layout/s.
        final Iterable<LayoutCodecProvider> codecProviders =
                ServiceLoader.load(LayoutCodecProvider.class);
        final LayoutCodecProvider codecProvider =
                codecProviders.iterator().next();
        final LayoutCodec layoutCodec = codecProvider.createLayoutCodec();

        final Iterable<LayoutStorageProvider> storageProviders =
                ServiceLoader.load(LayoutStorageProvider.class);
        final LayoutStorageProvider storageProvider =
                storageProviders.iterator().next();
        final LayoutStorage layoutStorage =
                storageProvider.createLayoutStorage(
                        layoutCodec.getExtension()
                );

        final Iterable<DockableProvider> dockableProviders =
                ServiceLoader.load(DockableProvider.class);

        dockableProvider = dockableProviders.iterator().next();

        final Iterable<LayoutPersistenceProvider> persistenceProviders =
                ServiceLoader.load(LayoutPersistenceProvider.class);
        final LayoutPersistenceProvider persistenceProvider =
                persistenceProviders.iterator().next();

        layoutSaver = persistenceProvider.createLayoutSaver(
                layoutStorage,
                layoutCodec
        );

        layoutRestorer = persistenceProvider.createLayoutRestorer(
                layoutStorage,
                layoutCodec,
                builder,
                dockableProvider
        );
    }

	@Override
	public void start(Stage stage) {

        stage.setWidth(1000);
        stage.setHeight(700);

    	DockContainerBranch branchRoot = builder.root("root");
		DockContainerBranch branchWorkspace = builder.branch("workspace");
		DockContainerLeaf leafWorkspaceTools = builder.leaf("workspace-tools");
		DockContainerLeaf leafWorkspaceHeaders = builder.leaf("workspace-headers");
		DockContainerLeaf leafTools = builder.leaf("misc-tools");

		branchWorkspace.setPruneWhenEmpty(false);
		leafWorkspaceTools.setPruneWhenEmpty(false);
		leafTools.setPruneWhenEmpty(false);
		leafTools.setPruneWhenEmpty(false);

		// Add dummy menus to each.
		leafTools.setMenuFactory(d -> addSideOptions(new ContextMenu(), leafTools));
		leafWorkspaceHeaders.setMenuFactory(d -> addSideOptions(new ContextMenu(), leafWorkspaceHeaders));
		leafWorkspaceTools.setMenuFactory(d -> addSideOptions(new ContextMenu(), leafWorkspaceTools));

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

		// Tools shouldn't allow splitting (mirroring intellij behavior)
		leafWorkspaceTools.setCanSplit(false);
		leafTools.setCanSplit(false);

		// Primary editor space should not prune when empty
		leafWorkspaceHeaders.setPruneWhenEmpty(false);

		// Set intended sizes for tools (leaf does not need to be a direct child, just some level down in the chain)
		branchRoot.setContainerSizePx(leafTools, 200);
		branchRoot.setContainerSizePx(leafWorkspaceTools, 300);

		// Make the bottom collapsed by default
		branchRoot.setContainerCollapsed(leafTools, true);

        // TODO BENTO-13: Move Dockable creation to BoxAppDockableProvider
        //  and acquire them using dockableProvider

		// Adding dockables to the leafs
		leafWorkspaceTools.addDockables(
				buildDockable(builder, 1, 0, "Workspace"),
				buildDockable(builder, 1, 1, "Bookmarks"),
				buildDockable(builder, 1, 2, "Modifications")
		);
		leafTools.addDockables(
				buildDockable(builder, 2, 0, "Logging"),
				buildDockable(builder, 2, 1, "Terminal"),
				buildDockable(builder, 2, 2, "Problems")
		);
		leafWorkspaceHeaders.addDockables(
				buildDockable(builder, 0, 0, "Class 1"),
				buildDockable(builder, 0, 1, "Class 2"),
				buildDockable(builder, 0, 2, "Class 3"),
				buildDockable(builder, 0, 3, "Class 4"),
				buildDockable(builder, 0, 4, "Class 5")
		);

		Scene scene = new Scene(branchRoot);
		scene.getStylesheets().add("/bento.css");
		stage.setScene(scene);
		stage.setOnHidden(e -> System.exit(0));
		stage.show();
	}

	@Nonnull
	private Dockable buildDockable(@Nonnull DockBuilding builder, int s, int i, @Nonnull String title) {
		Dockable dockable = builder.dockable();
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

	private void handleDockableClosing(@Nonnull DockEvent.DockableClosing closingEvent) {
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
			// simulate saving
			System.out.println("Saving " + dockable.getTitle() + "...");
		} else if (result.equals(ButtonType.NO)) {
			// nothing to do - just close
		} else if (result.equals(ButtonType.CANCEL)) {
			// prevent closing
			closingEvent.cancel();
		}
	}

	@Nonnull
	private static Shape makeIcon(int shapeMode, int i) {
		final int radius = 6;
		Shape icon = switch (shapeMode) {
			case 1 -> new Polygon(radius, 0, 0, radius * 2, radius * 2, radius * 2);
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
		icon.setEffect(new InnerShadow(BlurType.ONE_PASS_BOX, Color.BLACK, 2F, 10F, 0, 0));
		return icon;
	}

	@Nonnull
	private static ContextMenu addSideOptions(@Nonnull ContextMenu menu, @Nonnull DockContainerLeaf space) {
		for (Side side : Side.values()) {
			MenuItem item = new MenuItem(side.name());
			item.setGraphic(new Label(side == space.getSide() ? "✓" : " "));
			item.setOnAction(e -> space.setSide(side));
			menu.getItems().add(item);
		}
		return menu;
	}
}
