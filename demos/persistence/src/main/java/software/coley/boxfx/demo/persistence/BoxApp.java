package software.coley.boxfx.demo.persistence;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.LayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.impl.BentoLayout;
import software.coley.bentofx.persistence.impl.BentoLayout.BentoLayoutBuilder;
import software.coley.bentofx.persistence.impl.DockingLayout;
import software.coley.bentofx.persistence.impl.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.impl.provider.DockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.boxfx.demo.persistence.provider.BoxAppDockContainerLeafMenuFactoryProvider;
import software.coley.boxfx.demo.persistence.provider.BoxAppDockableMenuFactory;
import software.coley.boxfx.demo.persistence.provider.BoxAppDockableStateProvider;
import software.coley.boxfx.demo.persistence.provider.BoxAppStageIconImageProvider;
import software.coley.boxfx.demo.persistence.ui.MainStage;
import software.coley.boxfx.demo.persistence.ui.SecondDragDropStage;
import software.coley.boxfx.demo.persistence.ui.SecondStage;

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

    private final LayoutPersistenceProvider persistenceProvider =
            new DockingLayoutPersistenceProvider();

    private final DockableStateProvider dockableStateProvider =
            new BoxAppDockableStateProvider(
                    new BoxAppDockableMenuFactory()
            );

    private final StageIconImageProvider stageIconImageProvider =
            new BoxAppStageIconImageProvider();

    private final DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider =
            new BoxAppDockContainerLeafMenuFactoryProvider();

    private final DefaultBentoProvider bentoProvider =
            new DefaultBentoProvider();

    // The following fields are initialized in the start method.
    @SuppressWarnings("all")
    private MainStage mainStage;
    @SuppressWarnings("all")
    private SecondStage secondStage;
    @SuppressWarnings("all")
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

        // Do not hide the primary stage until after layouts to the other stages
        // have been applied; otherwise, the application may terminate (which is
        // the default behavior when all stages are closed, and the primary
        // stage is not visible).
        primaryStage.hide();
    }

    /**
     * @return if a prior {@link DockingLayout} has been saved, restores and
     * returns it. Otherwise, returns the default {@link DockingLayout}.
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

    private void applyDockingLayout(
            final DockingLayout dockingLayout
    ) {

        for (final BentoLayout bentoLayout :
                dockingLayout.getBentoLayouts()) {

            if (bentoLayout.matchesIdentity(mainStage.getBento())) {

                mainStage.restoreLayout(bentoLayout);

            } else if (bentoLayout.matchesIdentity(secondStage.getBento())) {

                secondStage.restoreLayout(bentoLayout);

            } else if (bentoLayout.matchesIdentity(secondDragDropStage.getBento())) {

                secondDragDropStage.restoreLayout(bentoLayout);

            } else {

                logger.warn(
                        "Unknown BentoLayout identifier: {}",
                        bentoLayout.getIdentifier()
                );
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
        final Bento mainStageBento = mainStage.getBento();
        BentoLayoutBuilder bentoLayoutBuilder = new BentoLayoutBuilder(
                mainStageBento.getIdentifier()
        );
        for (final DockContainerRootBranch rootBranch :
                mainStage.getRootBranches()) {
            bentoLayoutBuilder.addRootBranch(rootBranch);
        }
        dockingLayoutBuilder.addBentoLayout(bentoLayoutBuilder.build());

        // Second Stage (has no docking controls)
        final Bento secondStageBento = secondStage.getBento();
        bentoLayoutBuilder =
                new BentoLayoutBuilder(secondStageBento.getIdentifier());
        dockingLayoutBuilder.addBentoLayout(bentoLayoutBuilder.build());

        // Second DragDropStage
        final Parent parent = secondDragDropStage.getScene().getRoot();
        if (parent instanceof final DockContainerRootBranch rootBranch) {
            final BentoLayout bentoLayout = new BentoLayoutBuilder(
                    rootBranch.getBento().getIdentifier()
            )
                    .addDragDropStage(secondDragDropStage)
                    .build();
            dockingLayoutBuilder.addBentoLayout(bentoLayout);
        }

        return dockingLayoutBuilder.build();
    }
}
