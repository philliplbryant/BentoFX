/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl;

import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
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
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.codec.*;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Restores JavaFX stage layouts from a persisted {@link BentoState}.
 *
 * @author Phil Bryant
 */
public class BentoLayoutRestorer implements LayoutRestorer {

    private static final Logger logger = LoggerFactory.getLogger(BentoLayoutRestorer.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int UID_CAPACITY = 8;

    private final @NotNull LayoutStorage layoutStorage;
    private final @NotNull LayoutCodec layoutCodec;
    private final @NotNull DockableStateProvider dockableStateProvider;
    private final @Nullable StageIconImageProvider stageIconImageProvider;
    private final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider;

    public BentoLayoutRestorer(
            final @NotNull LayoutCodec layoutCodec,
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) {
        this.layoutCodec = Objects.requireNonNull(layoutCodec);
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
        this.dockableStateProvider = Objects.requireNonNull(dockableStateProvider);
        this.stageIconImageProvider = stageIconImageProvider;
        this.dockContainerLeafMenuFactoryProvider = dockContainerLeafMenuFactoryProvider;
    }

    @Override
    public boolean doesLayoutExist() {
        return layoutStorage.exists();
    }

    @Override
    public @NotNull DockContainerRootBranch restoreLayout(
            final @NotNull Stage primaryStage,
            final @NotNull Supplier<DockContainerRootBranch> defaultLayoutSupplier
    ) {

        try {

            // TODO BENTO-13: Persist and restore to size and position of the primary stage?

            primaryStage.hide();
            closeOtherStages(primaryStage);

            final CompletableFuture<BentoState> futureState =
                    new CompletableFuture<>();

            try (
                    final ScheduledExecutorService executorService =
                            newSingleThreadScheduledExecutor()
            ) {

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

            // Wait for the future to complete
            final BentoState bentoState = futureState.get();

            final Bento bento = new Bento(bentoState.getIdentifier());

            final DockBuilding dockBuilding = bento.dockBuilding();

            if (bentoState.getRootBranchStates().isEmpty()) {

                return dockBuilding.root("root-branch");

            } else {

                // Primary stage root branch is the one with no parent stage state.
                final Optional<DockContainerRootBranchState> optionalRootBranchState =
                        bentoState.getRootBranchStates().stream()
                                .filter(rb -> rb.getParent().isEmpty())
                                .findFirst();

                final DockContainerRootBranchState primaryRootBranchState =
                        optionalRootBranchState.orElse(null);

                final DockContainerRootBranch primaryRootBranch =
                        primaryRootBranchState == null ?
                                dockBuilding.root("empty-root-branch") :
                                restoreRootBranchContainer(
                                        dockBuilding,
                                        primaryRootBranchState
                                );

                // Restore secondary stages (root branches that have parent stage state)
                for (final DragDropStageState dragDropStageState : bentoState.getDragDropStageStates()) {
                    restoreDragDropStage(dockBuilding, dragDropStageState);
                }

                return primaryRootBranch;
            }
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

    private void closeOtherStages(final Stage primaryStage) {

        for (final Stage stage : FxStageUtils.getAllStages()) {

            if (stage != primaryStage) {

                stage.close();
            }
        }
    }

    private void restoreDragDropStage(
            final @NotNull DockBuilding dockBuilding,
            final @NotNull DragDropStageState stageState
    ) {

        final DragDropStage stage = new DragDropStage(
                stageState.isAutoClosedWhenEmpty()
        );

        if(stageIconImageProvider != null) {
            stage.getIcons().addAll(stageIconImageProvider.getStageIcons());
        }
        stageState.getTitle().ifPresent(stage::setTitle);
        stageState.getX().ifPresent(stage::setX);
        stageState.getY().ifPresent(stage::setY);
        stageState.getWidth().ifPresent(stage::setWidth);
        stageState.getHeight().ifPresent(stage::setHeight);

        // Iconified/fullscreen/maximized: apply what exists.
        stageState.isIconified().ifPresent(stage::setIconified);
        stageState.isFullScreen().ifPresent(stage::setFullScreen);
        stageState.isMaximized().ifPresent(stage::setMaximized);
        stageState.getModality().ifPresent(stage::initModality);

        stageState.getDockContainerRootBranchState().ifPresent(
                dockContainerRootBranchState -> {

                    final DockContainerRootBranch rootContainer =
                            restoreRootBranchContainer(
                                    dockBuilding,
                                    dockContainerRootBranchState
                            );

                    stage.setScene(new Scene(rootContainer));
                }
        );

        stage.show();
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

        if(dockContainerLeafMenuFactoryProvider != null) {
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

        state.isResizableWithParent().ifPresent(isResizableWithParent ->
                SplitPane.setResizableWithParent(
                        leaf,
                        isResizableWithParent
                )
        );

        state.getUncollapsedSizePx().ifPresent(size ->
                rootBranch.setContainerSizePx(leaf, size)
        );

        // FIXME BENTO-13: isCollapsed getting set but the leaf isn't collapsing
        //  What is the order in which Dockables should be added and
        //  setContainerCollapsed should be called?
        state.isCollapsed().ifPresent(isCollapsed -> {
                    logger.trace(
                            "Setting leaf {} collapsed to {}",
                            leaf.getIdentifier(),
                            isCollapsed
                    );
                    rootBranch.setContainerCollapsed(leaf, isCollapsed);
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
