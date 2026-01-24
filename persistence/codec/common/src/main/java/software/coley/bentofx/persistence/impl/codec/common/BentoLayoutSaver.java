/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.*;
import software.coley.bentofx.persistence.api.codec.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.OutputStream;
import java.util.Objects;

/**
 * Saves the layout of all JavaFX {@link Stage}s into a {@link BentoState} and
 * persists it via a {@link LayoutCodec}.
 *
 * @author Phil Bryant
 */
public final class BentoLayoutSaver implements LayoutSaver {

    private static final Logger logger =
            LoggerFactory.getLogger(BentoLayoutSaver.class);

    @NotNull private final Bento bento;
    @NotNull private final LayoutStorage layoutStorage;
    @NotNull private final LayoutCodec codec;

    public BentoLayoutSaver(
            final @NotNull Bento bento,
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull LayoutCodec layoutCodec
    ) {
        this.bento = Objects.requireNonNull(bento);
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
        this.codec = Objects.requireNonNull(layoutCodec);
    }

    @Override
    public void saveLayout() throws BentoStateException {

        final BentoStateBuilder bentoBuilder = new BentoStateBuilder();

        for (final Stage stage : FxStageUtils.getAllStages()) {


            try {
                DockContainerRootBranchState dockContainerRootBranchState =
                        createDockContainerRootBranchState(stage);
                if(dockContainerRootBranchState != null) {
                    bentoBuilder.addRootBranchState(dockContainerRootBranchState);
                }
            } catch (final Exception ex) {

                logger.error("Failed to save stage layout: {}", stage, ex);
            }
        }

        final BentoState state = bentoBuilder.build();

        try (final OutputStream out = layoutStorage.openOutputStream()) {

            codec.encode(state, out);
        } catch (final Exception ex) {

            throw new BentoStateException("Failed to encode BentoState", ex);
        }
    }

    private @Nullable DockContainerRootBranchState createDockContainerRootBranchState(
            final @NotNull Stage stage
    ) {
        final DockContainerRootBranch rootBranch = getDockContainerRootBranch(stage);

        if(rootBranch == null) {

            return null;
        } else {

            final String stageId = getStageId(stage);

            // Build drag-drop stage state - null for primary stages.
            final DragDropStageState dragDropStageState;
            if (stage instanceof final DragDropStage ddStage) {
                dragDropStageState =
                        new DragDropStageState.DragDropStageStateBuilder(
                                ddStage.isAutoCloseWhenEmpty()
                        )
                                .setTitle(stage.getTitle())
                                .setX(stage.getX())
                                .setY(stage.getY())
                                .setWidth(stage.getWidth())
                                .setHeight(stage.getHeight())
                                .setIsIconified(stage.isIconified())
                                .setIsFullScreen(stage.isFullScreen())
                                .setIsMaximized(stage.isMaximized())
                                // Important: Do NOT set dockContainerRootBranchState to avoid cyclic graphs
                                .setDockContainerRootBranchState(null)
                                .build();
            } else {
                dragDropStageState = null;
            }

            final DockContainerRootBranchStateBuilder rootBuilder =
                    new DockContainerRootBranchStateBuilder(stageId);
            rootBuilder.setParent(dragDropStageState);

            final DockContainerState branchState = saveDockContainer(rootBranch);
            rootBuilder.addDockContainerState(branchState);

            return rootBuilder.build();
        }
    }

    private @Nullable DockContainerRootBranch getDockContainerRootBranch(final @NotNull Stage stage) {

        for(final DockContainerRootBranch dockContainerRootBranch : bento.getRootContainers()) {

            if(isNodeInStage(stage, dockContainerRootBranch)) {
                return dockContainerRootBranch;
            }
        }

        return null;
    }

    private boolean isNodeInStage(
            final @NotNull Stage stage,
            final @NotNull Node node
    ) {
        return stage.getScene() == node.getScene();
    }


    private @NotNull DockContainerState saveDockContainer(
            final @NotNull DockContainer dockContainer
    ) {

        switch (dockContainer) {

            case final DockContainerRootBranch rootBranch -> {
                return saveBranch(rootBranch);
            }

            case final DockContainerBranch branch -> {
                return saveBranch(branch);
            }

            case final DockContainerLeaf leaf -> {
                return saveLeaf(leaf);
            }

            default -> {
                logger.warn("Unsupported node type: {}", dockContainer);
                // Fallback: empty leaf to keep the state valid.
                return new DockContainerLeafStateBuilder("leaf-empty").build();
            }
        }
    }

    private @NotNull DockContainerBranchState saveBranch(
            final @NotNull DockContainerBranch branch
    ) {
        final String id = nonEmptyOr(
                branch.getIdentifier(),
                "branch-" + System.identityHashCode(branch)
        );

        final DockContainerBranchStateBuilder builder =
                new DockContainerBranchStateBuilder(
                        id
                );

        builder.setOrientation(branch.orientationProperty().get());

        // Divider positions (supports multiple)
        final double[] positions = branch.getDividerPositions();

        for (int i = 0; i < positions.length; i++) {

            builder.addDividerPosition(i, positions[i]);
        }

        for (final DockContainer dockContainer : branch.getChildContainers()) {
            builder.addDockContainerState(saveDockContainer(dockContainer));
        }

        for (final Dockable dockable : branch.getDockables()) {
            builder.addChildDockableState(saveDockable(dockable));
        }

        return builder.build();
    }

    private @NotNull DockContainerLeafState saveLeaf(final @NotNull DockContainerLeaf leaf) {

        final String id = nonEmptyOr(
                leaf.getIdentifier(),
                "leaf-" + System.identityHashCode(leaf)
        );

        final DockContainerLeafStateBuilder leafStateBuilder =
                new DockContainerLeafStateBuilder(id);

        // Header side
        leafStateBuilder.setSide(leaf.getSide());

        // Selected dockable
        final Dockable selected = leaf.getSelectedDockable();

        if (selected != null) {

            leafStateBuilder.setSelectedDockableStateIdentifier(selected.getIdentifier());
        }

        // Dockables
        for (final Dockable dockable : leaf.getDockables()) {

            try {

                if (dockable != null) {
                    leafStateBuilder.addChildDockableState(
                            saveDockable(dockable)
                    );
                }
            } catch (final Exception ex) {

                logger.error("Failed to persist dockable in leaf {}", id, ex);
            }
        }

        return leafStateBuilder.build();
    }

    private DockableState saveDockable(final @NotNull Dockable dockable) {
        return new DockableStateBuilder(dockable.getIdentifier())
                .build();
    }

    private static String getStageId(final @NotNull Stage stage) {

        return "cstage:" + System.identityHashCode(stage);
    }

    private static String nonEmptyOr(
            final String value,
            final String fallback
    ) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
