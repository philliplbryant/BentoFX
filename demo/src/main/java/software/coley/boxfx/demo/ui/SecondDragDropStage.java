package software.coley.boxfx.demo.ui;

import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoLayout;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.boxfx.demo.provider.BoxAppBentoProvider;

import java.util.Objects;

import static software.coley.boxfx.demo.provider.BoxAppDockableStateProvider.SECOND_DOCKABLE_ID;
import static software.coley.boxfx.demo.ui.DockableUtils.createDockable;

/**
 * {@link DragDropStage} with docking controls to be persisted.
 */
public class SecondDragDropStage extends DragDropStage {

    private static final Logger logger =
            LoggerFactory.getLogger(SecondDragDropStage.class);

    private static final String ROOT_BRANCH_IDENTIFIER = "second-root-branch";

    private final Bento bento =
            new Bento("second-drag-drop-stage-bento");

    public SecondDragDropStage(
            final @NotNull BoxAppBentoProvider bentoProvider,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider
    ) {
        super(true);
        bentoProvider.addBento(bento);
        init(dockableStateProvider, stageIconImageProvider);
    }

    public Bento getBento() {
        return bento;
    }

    public void restoreLayout(final @NotNull BentoLayout bentoLayout) {

        for (final @NotNull DragDropStage dragDropStage :
                bentoLayout.getDragDropStages()) {

            if (dragDropStage.getScene().getRoot() instanceof
                    final DockContainerRootBranch rootBranch &&
                    Objects.equals(
                            rootBranch.getIdentifier(),
                            ROOT_BRANCH_IDENTIFIER
                    )) {
                // Apply the DragDropStage state to this Stage (ignore
                // autoCloseWhenEmpty; it can only be set when the
                // DragDropStage is initially created).
                setTitle(dragDropStage.getTitle());
                setX(dragDropStage.getX());
                setY(dragDropStage.getY());
                setWidth(dragDropStage.getWidth());
                setHeight(dragDropStage.getHeight());
                setOpacity(dragDropStage.getOpacity());
                setIconified(dragDropStage.isIconified());
                setFullScreen(dragDropStage.isFullScreen());
                setMaximized(dragDropStage.isMaximized());
                setAlwaysOnTop(dragDropStage.isAlwaysOnTop());
                setResizable(dragDropStage.isResizable());
                initModality(dragDropStage.getModality());
                if (dragDropStage.isFocused()) {
                    requestFocus();
                }
                show();
            } else {
                // The DragDropStage was created apart from the default
                // layout. Just show it.
                dragDropStage.show();
            }
        }
    }

    private void init(
            final @NotNull DockableStateProvider dockableStateProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider
    ) {
        final DockContainerLeaf leaf = bento.dockBuilding().leaf(
                "second-leaf"
        );

        dockableStateProvider.resolveDockableState(
                SECOND_DOCKABLE_ID
        ).ifPresentOrElse(
                dockableState ->
                        leaf.addDockable(createDockable(bento, dockableState)),
                () ->
                        logger.warn(
                                "Could not create DockableState for {} using " +
                                        "bento {}",
                                SECOND_DOCKABLE_ID,
                                bento
                        )
        );

        final DockContainerRootBranch rootBranch = new DockContainerRootBranch(
                bento,
                ROOT_BRANCH_IDENTIFIER
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
