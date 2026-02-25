package software.coley.boxfx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoLayout;
import software.coley.bentofx.persistence.api.BentoLayout.BentoLayoutBuilder;
import software.coley.bentofx.persistence.api.IdentifiableStageLayout;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.storage.DockingLayout;
import software.coley.bentofx.persistence.api.storage.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.impl.provider.BentoLayoutPersistenceProvider;
import software.coley.boxfx.demo.provider.*;
import software.coley.boxfx.demo.ui.MainStage;
import software.coley.boxfx.demo.ui.SecondDragDropStage;
import software.coley.boxfx.demo.ui.SecondStage;

import java.util.List;

import static software.coley.bentofx.control.IdentifiableStage.getIdentifiableStage;
import static software.coley.bentofx.persistence.impl.StageUtils.applyStageState;

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

    private final @NotNull BoxAppBentoProvider bentoProvider =
            new BoxAppBentoProvider();

    private MainStage mainStage;
    private SecondStage secondStage;
    private SecondDragDropStage secondDragDropStage;

    @Override
    public void start(Stage primaryStage) {

        mainStage = new MainStage(
                bentoProvider,
                dockableStateProvider,
                stageIconImageProvider,
                dockContainerLeafMenuFactoryProvider,
                this::saveDockingLayout
        );

        secondStage = new SecondStage(
                bentoProvider,
                stageIconImageProvider
        );

        secondDragDropStage = new SecondDragDropStage(
                bentoProvider,
                dockableStateProvider,
                stageIconImageProvider
        );

        DockingLayout dockingLayout = getDockingLayout();
        applyDockingLayout(dockingLayout);

        // Ignore, but do not close, the primary stage. The application will
        // terminate if all stages are closed, and the primary stage is not
        // visible.
        primaryStage.hide();

        mainStage.show();
        secondStage.show();
    }

    /**
     * @return if a prior {@link DockingLayout} has been saved, restores and
     * returns it. Otherwise, returns the default {@link DockingLayout}.
     * @see #getDefaultDockingLayout()
     */
    private @NotNull DockingLayout getDockingLayout() {

        @NotNull DockingLayout dockingLayout;

        final LayoutRestorer layoutRestorer =
                persistenceProvider.getLayoutRestorer(
                        bentoProvider,
                        DEFAULT_LAYOUT_IDENTIFIER,
                        dockableStateProvider,
                        stageIconImageProvider,
                        dockContainerLeafMenuFactoryProvider
                );

        if (layoutRestorer.doesLayoutExist()) {

            return layoutRestorer.restoreLayout(
                    this::getDefaultDockingLayout
            );

        } else {

            dockingLayout = getDefaultDockingLayout();
        }

        return dockingLayout;
    }

    private void applyDockingLayout(
            final @NotNull DockingLayout dockingLayout
    ) {

        for (final BentoLayout bentoLayout : dockingLayout.getBentoLayouts()) {

            for (final IdentifiableStageLayout stageLayout :
                    bentoLayout.getStageLayouts()) {

                getIdentifiableStage(
                        stageLayout.getIdentifier()
                ).ifPresent(stage -> {

                    // Add each root branch to the scene.
                    // This application only has one root branch so just get
                    // and add the first one to a new scene.
                    final List<DockContainerRootBranch> rootBranches =
                            stageLayout.getRootBranches();

                    if (!rootBranches.isEmpty()) {
                        final Scene scene =
                                new Scene(rootBranches.getFirst());
                        scene.getStylesheets().add("/bento.css");
                        stage.setScene(scene);
                    }

                    // Wait for dockables to be initialized and the scene set
                    // before applying the layout; otherwise, the layout might
                    // be changed by the added controls.
                    applyStageState(stageLayout.getStageState(), stage);
                    stage.show();
                });
            }

            for (final @NotNull DragDropStage dragDropStage :
                    bentoLayout.getDragDropStages()) {
                dragDropStage.show();
            }
        }

    }

    private void saveDockingLayout() {
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

    private DockingLayout getDefaultDockingLayout() {

        DockingLayoutBuilder dockingLayoutBuilder =
                new DockingLayoutBuilder();

        // Main Stage
        mainStage.centerOnScreen();
        BentoLayoutBuilder bentoLayoutBuilder = new BentoLayoutBuilder(
                mainStage.getBento().getIdentifier()
        );
        bentoLayoutBuilder.addStageLayout(mainStage.getLayout());
        dockingLayoutBuilder.addBentoLayout(bentoLayoutBuilder.build());

        // Second Stage
        bentoLayoutBuilder = new BentoLayoutBuilder(
                secondStage.getBento().getIdentifier()
        );
        bentoLayoutBuilder.addStageLayout(secondStage.getLayout());
        dockingLayoutBuilder.addBentoLayout(bentoLayoutBuilder.build());

        // Second DragDropStage
        final @NotNull BentoLayout bentoLayout = new BentoLayoutBuilder(
                secondDragDropStage.getBento().getIdentifier()
        )
                .addDragDropStage(secondDragDropStage)
                .build();
        dockingLayoutBuilder.addBentoLayout(bentoLayout);

        return dockingLayoutBuilder.build();
    }
}
