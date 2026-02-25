package software.coley.boxfx.demo.ui;

import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.boxfx.demo.provider.BoxAppBentoProvider;

import static software.coley.bentofx.persistence.api.PersistableStage.createDockable;
import static software.coley.boxfx.demo.provider.BoxAppDockableStateProvider.SECOND_DOCKABLE_ID;

/**
 * {@link DragDropStage} with docking controls to be persisted.
 */
public class SecondDragDropStage extends DragDropStage {

    private static final Logger logger =
            LoggerFactory.getLogger(SecondDragDropStage.class);

    public SecondDragDropStage(
            final @NotNull BoxAppBentoProvider bentoProvider,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider
    ) {
        super(
                new Bento("second-drag-drop-stage-bento"),
                "second-drag-drop-stage",
                true)
        ;
        bentoProvider.addBento(getBento());
        init(dockableStateProvider, stageIconImageProvider);
    }

    private void init(
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider
    ) {
        final DockContainerLeaf leaf = getBento().dockBuilding().leaf(
                "second-leaf"
        );

        dockableStateProvider.resolveDockableState(
                SECOND_DOCKABLE_ID
        ).ifPresentOrElse(
                dockableState ->
                        leaf.addDockable(createDockable(getBento(), dockableState)),
                () ->
                        logger.warn(
                                "Could not create DockableState for {} using " +
                                        "bento {}",
                                SECOND_DOCKABLE_ID,
                                getBento()
                        )
        );

        final DockContainerRootBranch rootBranch = new DockContainerRootBranch(
                getBento(),
                "second-root-branch"
        );
        rootBranch.addContainer(leaf);

        setScene(new Scene(rootBranch));
        setTitle("Second DragDropStage");
        getIcons().addAll(
                stageIconImageProvider.getStageIcons()
        );
        setWidth(325);
        setHeight(175);
        setX(-325);
        setY(175);
    }
}
