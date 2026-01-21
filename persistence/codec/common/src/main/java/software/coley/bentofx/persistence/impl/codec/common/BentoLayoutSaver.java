/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.codec.BentoState;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.DockContainerBranchState;
import software.coley.bentofx.persistence.api.codec.DockContainerLeafState;
import software.coley.bentofx.persistence.api.codec.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerRootBranchState;
import software.coley.bentofx.persistence.api.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerState;
import software.coley.bentofx.persistence.api.codec.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.codec.DragDropStageState;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.OutputStream;
import java.util.Objects;

import static software.coley.bentofx.persistence.impl.codec.common.FxStageUtils.IS_PRIMARY_STAGE_PROPERTY_KEY_NAME;
import static software.coley.bentofx.persistence.impl.codec.common.FxStageUtils.PRIMARY_STAGE_ID;
import static software.coley.bentofx.persistence.impl.codec.common.FxStageUtils.STAGE_ID_PROPERTY_KEY_NAME;

/**
 * Saves the layout of all JavaFX {@link Stage}s into a {@link BentoState} and
 * persists it via a {@link LayoutCodec}.
 */
public final class BentoLayoutSaver implements LayoutSaver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BentoLayoutSaver.class);

    private final @NotNull LayoutStorage layoutStorage;
    private final @NotNull LayoutCodec codec;

    public BentoLayoutSaver(
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull LayoutCodec layoutCodec
    ) {
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
        this.codec = Objects.requireNonNull(layoutCodec);
    }

    @Override
    public void saveLayout() throws BentoStateException {

        final BentoState.BentoStateBuilder bentoBuilder =
                new BentoState.BentoStateBuilder("bento");

        for (final Stage stage : FxStageUtils.getAllStages()) {

            try {

                bentoBuilder.addRootBranchState(saveStage(stage));
            } catch (final Exception ex) {

                LOGGER.error("Failed to save stage layout: {}", stage, ex);
            }
        }

        final BentoState state = bentoBuilder.build();

        try (final OutputStream out = layoutStorage.openOutputStream()) {

            codec.encode(state, out);
        } catch (final Exception ex) {

            throw new BentoStateException("Failed to encode BentoState", ex);
        }
    }

    private DockContainerRootBranchState saveStage(
            final @NotNull Stage stage
    ) {
        final String stageId = getStageId(stage);
        final boolean isPrimary = isPrimaryStage(stage);

        // Build drag-drop stage state for non-primary stages.
        final DragDropStageState dragDropStageState;
        if (!isPrimary && stage instanceof final DragDropStage ddStage) {
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

        final Node primaryNode = getPrimaryNode(stage);

        final DockContainerRootBranchStateBuilder rootBuilder =
                new DockContainerRootBranchStateBuilder(
                        isPrimary ? PRIMARY_STAGE_ID : stageId
                ).setParent(dragDropStageState);

        final DockContainerState dockContainerState =
                saveDockContainer(primaryNode);

        switch (dockContainerState) {
            case final DockContainerBranchState branchState -> rootBuilder.addDockContainerBranchState(branchState);
            case final DockContainerLeafState leafState -> rootBuilder.addDockContainerLeafState(leafState);
            case null, default -> LOGGER.warn("Unsupported root container state type: {}", dockContainerState);
        }

        return rootBuilder.build();
    }

    private DockContainerState saveDockContainer(final @NotNull Node node) {

        switch (node) {

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
                LOGGER.warn("Unsupported node type: {}", node);
                // Fallback: empty leaf to keep the state valid.
                return new DockContainerLeafStateBuilder("leaf-empty").build();
            }
        }
    }

    private DockContainerBranchState saveBranch(
            final @NotNull DockContainerBranch branch
    ) {
        final String id = nonEmptyOr(
                branch.getIdentifier(),
                "branch-" + System.identityHashCode(branch)
        );

        final DockContainerBranchState.DockContainerBranchStateBuilder builder =
                new DockContainerBranchState.DockContainerBranchStateBuilder(
                        id
                );

        builder.setOrientation(branch.orientationProperty().get());

        // Divider positions (supports multiple)
        final double[] positions = branch.getDividerPositions();

        for (int i = 0; i < positions.length; i++) {

            builder.addDividerPosition(i, positions[i]);
        }

        for (final Node item : branch.getItems()) {
            builder.addDockContainerState(saveDockContainer(item));
        }

        return builder.build();
    }

    private DockContainerLeafState saveLeaf(final @NotNull DockContainerLeaf leaf) {

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
                    leafStateBuilder.addDockableState(
                            new DockableStateBuilder(dockable.getIdentifier())
                                    .build()
                    );
                }
            } catch (final Exception ex) {

                LOGGER.error("Failed to persist dockable in leaf {}", id, ex);
            }
        }

        return leafStateBuilder.build();
    }

    private static boolean isPrimaryStage(final @NotNull Stage stage) {

        final Object isPrimaryStagePropertyValue =
                stage.getProperties().get(IS_PRIMARY_STAGE_PROPERTY_KEY_NAME);

        return Boolean.TRUE.equals(isPrimaryStagePropertyValue);
    }

    private static String getStageId(final @NotNull Stage stage) {

        final Object stageIdPropertyValue =
                stage.getProperties().get(STAGE_ID_PROPERTY_KEY_NAME);

        return stageIdPropertyValue instanceof final String id &&
                !id.isBlank() ?
                id :
                "stage-" + System.identityHashCode(stage);
    }

    // FIXME BENTO-13: Save the DockContainerRootBranch instead of the Node in
    //  the center of the layout
    private static Node getPrimaryNode(
            final @NotNull Stage stage
    ) {
        final Scene scene = stage.getScene();
        final Parent root = scene.getRoot();

        if (root instanceof final BorderPane border) {

            final Node center = border.getCenter();
            if (center != null) {

                return center;
            }
        }

        return root;
    }

    private static String nonEmptyOr(
            final String value,
            final String fallback
    ) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
