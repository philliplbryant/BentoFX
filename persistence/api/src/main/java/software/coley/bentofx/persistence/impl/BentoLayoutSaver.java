/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.control.IdentifiableStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.PersistableStage;
import software.coley.bentofx.persistence.api.codec.*;
import software.coley.bentofx.persistence.api.codec.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.codec.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.api.codec.IdentifiableStageState.StageStateBuilder;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static software.coley.bentofx.control.IdentifiableStage.getAllIdentifiableStages;
import static software.coley.bentofx.persistence.impl.StageUtils.getStageStateBuilder;

/**
 * Saves the layout of all JavaFX {@link Stage}s into a {@link BentoState} and
 * persists it via a {@link LayoutCodec}.
 *
 * @author Phil Bryant
 */
public class BentoLayoutSaver extends AbstractAutoCloseableLayoutSaver {

    private static final Logger logger =
            LoggerFactory.getLogger(BentoLayoutSaver.class);

    @NotNull
    private final LayoutStorage layoutStorage;
    @NotNull
    private final LayoutCodec layoutCodec;

    public BentoLayoutSaver(
            final @NotNull BentoProvider bentoProvider,
            final @NotNull LayoutCodec layoutCodec,
            final @NotNull LayoutStorage layoutStorage
    ) {
        super(bentoProvider);
        this.layoutCodec = Objects.requireNonNull(layoutCodec);
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
    }

    @Override
    public void saveLayout() throws BentoStateException {

        final @NotNull List<@NotNull BentoState> bentoStateList =
                new ArrayList<>();

        for (Bento bento : bentoProvider.getAllBentos()) {

            final BentoStateBuilder bentoStateBuilder =
                    new BentoStateBuilder(bento.getIdentifier());

            for (final IdentifiableStage stage : getAllIdentifiableStages()) {

                switch (Objects.requireNonNull(stage)) {
                    case DragDropStage dragDropStage -> saveDragDropStage(
                            dragDropStage,
                            bento,
                            bentoStateBuilder
                    );
                    case PersistableStage persistableStage -> savePersistableStage(
                            persistableStage,
                            bento,
                            bentoStateBuilder
                    );
                    default -> logger.warn("Unknown stage type: {}", stage.getClass());
                }
            }

            bentoStateList.add(bentoStateBuilder.build());
        }

        try (final OutputStream out = layoutStorage.openOutputStream()) {

            layoutCodec.encode(bentoStateList, out);
        } catch (final Exception ex) {

            throw new BentoStateException("Failed to encode BentoState", ex);
        }

    }

    private void saveDragDropStage(
            DragDropStage dragDropStage,
            Bento bento,
            BentoStateBuilder bentoStateBuilder
    ) {
        // Only add the DragDropStages for the specified Bento
        if (Objects.equals(
                dragDropStage.getBento().getIdentifier(),
                bento.getIdentifier()
        )) {
            final @Nullable DockContainerRootBranch rootBranch =
                    getDockContainerRootBranch(bento, dragDropStage);

            final @Nullable Bento rootBranchBento =
                    rootBranch == null
                            ? null :
                            rootBranch.getBento();

            if (rootBranchBento != null) {

                final @Nullable DockContainerRootBranchState rootBranchState =
                        getRootBranchState(rootBranch);

                if (rootBranchState != null) {

                    bentoStateBuilder.addDragDropStageState(
                            new DragDropStageStateBuilder(
                                    dragDropStage.getIdentifier(),
                                    dragDropStage.isAutoCloseWhenEmpty()
                            )
                                    .setTitle(dragDropStage.getTitle())
                                    .setX(dragDropStage.getX())
                                    .setY(dragDropStage.getY())
                                    .setWidth(dragDropStage.getWidth())
                                    .setHeight(dragDropStage.getHeight())
                                    .setModality(dragDropStage.getModality())
                                    .setOpacity(dragDropStage.getOpacity())
                                    .setIconified(dragDropStage.isIconified())
                                    .setFullScreen(dragDropStage.isFullScreen())
                                    .setMaximized(dragDropStage.isMaximized())
                                    .setAlwaysOnTop(dragDropStage.isAlwaysOnTop())
                                    .setResizable(dragDropStage.isResizable())
                                    .setShowing(dragDropStage.isShowing())
                                    .setFocused(dragDropStage.isFocused())
                                    .setDockContainerRootBranchState(
                                            rootBranchState
                                    )
                                    .build()
                    );
                }
            }
        }
    }

    private void savePersistableStage(
            PersistableStage stage,
            Bento bento,
            BentoStateBuilder bentoStateBuilder
    ) {
        // Only add the stages for the specified Bento
        if (Objects.equals(
                stage.getBento().getIdentifier(),
                bento.getIdentifier()
        )
        ) {
            StageStateBuilder stageStateBuilder =
                    getStageStateBuilder(stage);

            final DockContainerRootBranch rootBranch =
                    getDockContainerRootBranch(bento, stage);
            if (rootBranch != null) {
                final @Nullable DockContainerRootBranchState rootBranchState =
                        getRootBranchState(rootBranch);
                if (rootBranchState != null) {
                    stageStateBuilder.addRootBranchState(rootBranchState);
                }
            }

            bentoStateBuilder.addIdentifiableStageState(
                    stageStateBuilder.build()
            );
        }
    }

    // FIXME BENTO-13: A DragDropStage can only have one root branch but
    //  there's nothing to prevent a Stage from having more than one.
    private @Nullable DockContainerRootBranch getDockContainerRootBranch(
            final @NotNull Bento bento,
            final @NotNull Stage stage
    ) {
        for (final DockContainerRootBranch dockContainerRootBranch :
                bento.getRootContainers()) {

            if (isNodeInStage(stage, dockContainerRootBranch)) {
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

            case final DockContainerBranch branch -> {

                return getBranchState(branch);
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

    private @Nullable DockContainerRootBranchState getRootBranchState(
            final @Nullable DockContainerRootBranch branch
    ) {
        if (branch == null) {

            return null;
        } else {

            final DockContainerRootBranchStateBuilder builder =
                    new DockContainerRootBranchStateBuilder(
                            branch.getIdentifier()
                    );

            setCommonDockContainerBranchProperties(builder, branch);
            return builder.build();
        }
    }

    private @NotNull DockContainerBranchState getBranchState(
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

        setCommonDockContainerBranchProperties(builder, branch);

        for (final Dockable dockable : branch.getDockables()) {
            builder.addChildDockableState(saveDockable(dockable));
        }

        return builder.build();
    }

    private void setCommonDockContainerBranchProperties(
            final @NotNull DockContainerBranchStateBuilder builder,
            final @NotNull DockContainerBranch branch
    ) {

        builder.setPruneWhenEmpty(branch.doPruneWhenEmpty());

        builder.setOrientation(branch.orientationProperty().get());

        // Divider positions (supports multiple)
        final double[] positions = branch.getDividerPositions();

        for (int i = 0; i < positions.length; i++) {

            builder.addDividerPosition(i, positions[i]);
        }

        for (final DockContainer dockContainer : branch.getChildContainers()) {
            builder.addDockContainerState(saveDockContainer(dockContainer));
        }
    }

    private @NotNull DockContainerLeafState saveLeaf(final @NotNull DockContainerLeaf leaf) {

        final String id = nonEmptyOr(
                leaf.getIdentifier(),
                "leaf-" + System.identityHashCode(leaf)
        );

        final DockContainerLeafStateBuilder leafStateBuilder =
                new DockContainerLeafStateBuilder(id);

        leafStateBuilder.setPruneWhenEmpty(leaf.doPruneWhenEmpty());

        leafStateBuilder.setSide(leaf.getSide());

        leafStateBuilder.setResizableWithParent(leaf.isResizable());

        leafStateBuilder.setCanSplit(leaf.isCanSplit());

        leafStateBuilder.setUncollapsedSizePx(leaf.getUncollapsedSize());

        leafStateBuilder.setCollapsed(leaf.isCollapsed());

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

    private static String nonEmptyOr(
            final String value,
            final String fallback
    ) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
