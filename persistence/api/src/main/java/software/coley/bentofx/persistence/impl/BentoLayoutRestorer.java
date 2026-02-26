/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl;

import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoLayout.BentoLayoutBuilder;
import software.coley.bentofx.persistence.api.IdentifiableStageLayout;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.codec.*;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.storage.DockingLayout;
import software.coley.bentofx.persistence.api.storage.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static software.coley.bentofx.persistence.impl.StageUtils.applyStageState;

/**
 * Restores JavaFX stage layouts from a persisted {@link BentoState}.
 *
 * @author Phil Bryant
 */
public class BentoLayoutRestorer implements LayoutRestorer {

    private static final Logger logger = LoggerFactory.getLogger(BentoLayoutRestorer.class);

    private final @NotNull LayoutCodec layoutCodec;
    private final @NotNull LayoutStorage layoutStorage;
    private final @NotNull BentoProvider bentoProvider;
    private final @NotNull DockableStateProvider dockableStateProvider;
    private final @Nullable StageIconImageProvider stageIconImageProvider;
    private final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider;

    public BentoLayoutRestorer(
            final @NotNull LayoutCodec layoutCodec,
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull BentoProvider bentoProvider,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) {
        this.layoutCodec = Objects.requireNonNull(layoutCodec);
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
        this.bentoProvider = Objects.requireNonNull(bentoProvider);
        this.dockableStateProvider = Objects.requireNonNull(dockableStateProvider);
        this.stageIconImageProvider = stageIconImageProvider;
        this.dockContainerLeafMenuFactoryProvider = dockContainerLeafMenuFactoryProvider;
    }

    @Override
    public boolean doesLayoutExist() {
        return layoutStorage.exists();
    }

    @Override
    public @NotNull DockingLayout restoreLayout(
            final @NotNull Supplier<DockingLayout> defaultLayoutSupplier
    ) {
        final DockingLayoutBuilder dockingLayoutBuilder =
                new DockingLayoutBuilder();
        try {

            final CompletableFuture<@NotNull List<@NotNull BentoState>> futureState =
                    new CompletableFuture<>();

            scheduleService(futureState);

            // Wait for the future to complete
            List<BentoState> bentoStateList = futureState.get();

            for (BentoState bentoState : bentoStateList) {

                final String bentoId = bentoState.getIdentifier();

                final BentoLayoutBuilder bentoLayoutBuilder =
                        new BentoLayoutBuilder(bentoId);

                // All Bentos are not created equal - it's possible the client
                // application extended the Bento class or otherwise customized
                // its functionality, and used the custom Bento when creating
                // the layout that is being restored.
                final Bento bento = bentoProvider.getBento(bentoId)
                        .orElseGet(() -> {
                                    logger.warn(
                                            "Could not find the Bento with identifier {}. " +
                                                    "Some docking features might not be available.",
                                            bentoId
                                    );
                                    return new Bento();
                                }
                        );

                final DockBuilding dockBuilding = bento.dockBuilding();

                for (final IdentifiableStageState identifiableStageState :
                        bentoState.getIdentifiableStageStates()) {
                    bentoLayoutBuilder.addStageLayout(
                            restoreIdentifiableStageLayout(
                                    dockBuilding,
                                    identifiableStageState
                            )
                    );
                }

                // Restore DragDropStages
                for (final DragDropStageState dragDropStageState :
                        bentoState.getDragDropStageStates()) {
                    bentoLayoutBuilder.addDragDropStage(
                            restoreDragDropStage(
                                    bento,
                                    dockBuilding,
                                    dragDropStageState
                            )
                    );
                }

                dockingLayoutBuilder.addBentoLayout(
                        bentoLayoutBuilder.build()
                );
            }

            return dockingLayoutBuilder.build();

        } catch (final ExecutionException e) {

            logger.warn(
                    "An error occurred while attempting to read the layout",
                    e
            );

            return defaultLayoutSupplier.get();

        } catch (final InterruptedException e) {

            Thread.currentThread().interrupt();
            logger.warn(
                    "Interrupted while attempting to read the layout",
                    e
            );

            return defaultLayoutSupplier.get();
        }
    }

    private void scheduleService(
            final CompletableFuture<@NotNull List<@NotNull BentoState>> futureState
    ) {
        try (final ScheduledExecutorService executorService =
                     newSingleThreadScheduledExecutor()) {

            // Do not decode on the application thread
            executorService.schedule(
                    () -> {
                        try (
                                final InputStream in =
                                        layoutStorage.openInputStream()
                        ) {

                            futureState.complete(layoutCodec.decode(in));
                        } catch (final BentoStateException | IOException e) {
                            futureState.completeExceptionally(e);
                        }
                    },
                    300,
                    MILLISECONDS
            );
        }
    }

    private @NotNull IdentifiableStageLayout restoreIdentifiableStageLayout(
            final @NotNull DockBuilding dockBuilding,
            final @NotNull IdentifiableStageState stageState
    ) {
        final @NotNull List<@NotNull DockContainerRootBranch> rootBranches =
                new ArrayList<>();

        for (DockContainerRootBranchState rootBranchState :
                stageState.getRootBranchStates()) {
            rootBranches.add(
                    restoreRootBranchContainer(dockBuilding, rootBranchState)
            );
        }

        return new IdentifiableStageLayout(
                stageState,
                rootBranches
        );
    }

    private @NotNull DragDropStage restoreDragDropStage(
            final @NotNull Bento bento,
            final @NotNull DockBuilding dockBuilding,
            final @NotNull DragDropStageState stageState
    ) {

        final DragDropStage dragDropStage = new DragDropStage(
                bento,
                stageState.getIdentifier(),
                stageState.isAutoClosedWhenEmpty()
        );

        stageState.getDockContainerRootBranchState().ifPresent(
                dockContainerRootBranchState -> {

                    final DockContainerRootBranch rootContainer =
                            restoreRootBranchContainer(
                                    dockBuilding,
                                    dockContainerRootBranchState
                            );

                    dragDropStage.setScene(new Scene(rootContainer));
                }
        );

        if (stageIconImageProvider != null) {
            dragDropStage.getIcons().addAll(stageIconImageProvider.getStageIcons());
        }

        applyStageState(stageState, dragDropStage);

        return dragDropStage;
    }

    private @NotNull DockContainerRootBranch restoreRootBranchContainer(
            final @NotNull DockBuilding dockBuilding,
            final @NotNull DockContainerRootBranchState rootBranchState
    ) {
        final DockContainerRootBranch rootBranch =
                dockBuilding.root(rootBranchState.getIdentifier());

        rootBranchState.doPruneWhenEmpty().ifPresent(
                rootBranch::setPruneWhenEmpty
        );

        rootBranchState.getOrientation().ifPresent(
                rootBranch::setOrientation
        );

        for (Map.Entry<@NotNull Integer, @NotNull Double> positionEntry :
                rootBranchState.getDividerPositions().entrySet()) {
            rootBranch.setDividerPosition(
                    positionEntry.getKey(),
                    positionEntry.getValue()
            );
        }

        for (final DockContainerState childState : rootBranchState.getChildDockContainerStates()) {

            final DockContainer dockContainer = restoreDockContainer(rootBranch, childState);

            if (dockContainer != null) {

                rootBranch.addContainer(dockContainer);
            }
        }

        for (final DockableState dockableState : rootBranchState.getChildDockableStates()) {

            final Dockable dockable =
                    restoreDockable(
                            dockBuilding,
                            dockableState.getIdentifier()
                    );

            if (dockable != null) {
                rootBranch.addDockable(dockable);
            }
        }

        return rootBranch;
    }

    private @Nullable DockContainer restoreDockContainer(
            final @NotNull DockContainerRootBranch rootBranch,
            final @NotNull DockContainerState state
    ) {

        switch (state) {
            case final DockContainerBranchState branchState -> {

                return restoreBranch(rootBranch, branchState);
            }
            case final DockContainerLeafState leafState -> {

                return restoreLeaf(rootBranch, leafState);
            }
            default -> {

                logger.warn("Unknown DockContainerState type: {}", state.getClass());
                return null;
            }
        }
    }

    private @NotNull DockContainerBranch restoreBranch(
            final @NotNull DockContainerRootBranch rootBranch,
            final @NotNull DockContainerBranchState branchState
    ) {
        final String id = branchState.getIdentifier();

        final DockContainerBranch branch = rootBranch.getBento()
                .dockBuilding()
                .branch(id);

        branchState.doPruneWhenEmpty().ifPresent(branch::setPruneWhenEmpty);

        // Orientation
        branchState.getOrientation().ifPresent(orientation ->
                branch.orientationProperty().set(orientation)
        );

        // Children
        for (
                final DockContainerState dockContainerState :
                branchState.getChildDockContainerStates()
        ) {
            final DockContainer container =
                    restoreDockContainer(rootBranch, dockContainerState);

            if (container != null) {

                branch.addContainer(container);
            } else {

                logger.warn(
                        "Attempting to restore null DockContainer from " +
                                "DockContainerState: {}",
                        dockContainerState.getClass()
                );
            }
        }

        // Divider positions
        for (Map.Entry<@NotNull Integer, @NotNull Double> positionEntry :
                branchState.getDividerPositions().entrySet()) {
            branch.setDividerPosition(
                    positionEntry.getKey(),
                    positionEntry.getValue()
            );
        }

        return branch;
    }

    private @NotNull DockContainerLeaf restoreLeaf(
            final @NotNull DockContainerRootBranch rootBranch,
            final @NotNull DockContainerLeafState state
    ) {
        final String id = state.getIdentifier();

        final DockBuilding dockBuilding = rootBranch.getBento().dockBuilding();
        final DockContainerLeaf leaf = dockBuilding.leaf(id);

        if (dockContainerLeafMenuFactoryProvider != null) {
            dockContainerLeafMenuFactoryProvider.createDockContainerLeafMenuFactory(
                    leaf.getIdentifier()
            ).ifPresent(
                    leaf::setMenuFactory
            );
        }

        state.doPruneWhenEmpty().ifPresent(leaf::setPruneWhenEmpty);

        state.getSide().ifPresent(leaf::setSide);

        state.isCanSplit().ifPresent(leaf::setCanSplit);

        final String selectedId =
                state.getSelectedDockableIdentifier().orElse(null);

        state.isResizableWithParent().ifPresent(isResizableWithParent ->
                SplitPane.setResizableWithParent(
                        leaf,
                        isResizableWithParent
                )
        );

        state.getUncollapsedSizePx().ifPresent(size ->
                rootBranch.setContainerSizePx(leaf, size)
        );

        for (final DockableState dockableState : state.getChildDockableStates()) {

            final String dockableId = dockableState.getIdentifier();

            final Dockable dockable = restoreDockable(
                    dockBuilding,
                    dockableId
            );

            if (dockable != null) {

                leaf.addDockable(dockable);
                if (dockableId.equals(selectedId)) {
                    leaf.selectDockable(dockable);
                }
            } else {

                logger.warn("Dockable with ID '{}' could not be acquired.", dockableId);
            }
        }

        // FIXME BENTO-13: Even though isCollapsed is getting set, the leaf
        //  isn't collapsing. According to notes in
        //  DockContainerBranch#setContainerCollapsed, collapsing can only occur
        //  if there is a splitter between two or more child containers. We've
        //  already created the rootBranch DockContainer added the Dockable to
        //  the leaf. Restoring the docking components in the same order as
        //  specified in MainStage doesn't seem to help either.
        state.isCollapsed().ifPresent(isCollapsed -> {
                    logger.trace(
                            "Setting leaf {} collapsed to {}",
                            leaf.getIdentifier(),
                            isCollapsed
                    );
                    final boolean wasCollapsed =
                            rootBranch.setContainerCollapsed(leaf, isCollapsed);
                    logger.trace(
                            "Leaf {} {} collapsed.",
                            leaf.getIdentifier(),
                            wasCollapsed ? "WAS" : "was NOT"
                    );
                }
        );

        return leaf;
    }

    private @Nullable Dockable restoreDockable(
            @NotNull DockBuilding dockBuilding,
            @NotNull String dockableIdentifier
    ) {

        final @NotNull Optional<DockableState> optionalDockableProvider =
                dockableStateProvider.resolveDockableState(dockableIdentifier);

        final @Nullable DockableState dockableState = optionalDockableProvider.orElse(null);

        Dockable dockable;

        if (dockableState != null) {

            dockable = dockBuilding.dockable(dockableIdentifier);

            dockableState.getTitle().ifPresent(
                    dockable::setTitle
            );

            dockableState.getDockableIconFactory().ifPresent(
                    dockable::setIconFactory
            );

            dockableState.getDockableNode().ifPresent(
                    dockable::setNode
            );

            dockableState.getDockableMenuFactory().ifPresent(
                    dockable::setContextMenuFactory
            );

            dockableState.getDragGroupMask().ifPresent(
                    dockable::setDragGroupMask
            );

            dockableState.isClosable().ifPresent(
                    dockable::setClosable
            );

            dockableState.getDockableConsumer().ifPresent(consumer ->
                    consumer.accept(dockable)
            );

        } else {

            dockable = null;
        }

        return dockable;
    }
}
