/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common;

import javafx.scene.Parent;
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
import software.coley.bentofx.persistence.api.provider.DockableMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableProvider;
import software.coley.bentofx.persistence.api.provider.ImageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

// TODO BENTO-13: Restore using DockContainerLeafMenuFactory (see BoxApp)

// TODO BENTO-13: Restore using DockableMenuFactory

// FIXME BENTO-13: Persistence is wrapping "root" in an unnecessary/extra branch
//  with no divider positions set.

/**
 * Restores JavaFX stage layouts from a persisted {@link BentoState}.
 *
 * @author Phil Bryant
 */
public final class BentoLayoutRestorer implements LayoutRestorer {

    private static final Logger logger = LoggerFactory.getLogger(BentoLayoutRestorer.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int UID_CAPACITY = 8;

    @NotNull
    private final LayoutStorage layoutStorage;
    @NotNull
    private final LayoutCodec codec;
    @NotNull
    private final DockBuilding dockBuilding;
    @NotNull
    private final DockableProvider dockableProvider;
    @NotNull
    private final ImageProvider imageProvider;
    @Nullable
    private final DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider;
    @Nullable
    private final DockableMenuFactoryProvider dockableMenuFactoryProvider;

    public BentoLayoutRestorer(
            final @NotNull Bento bento,
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull LayoutCodec codec,
            final @NotNull DockableProvider dockableProvider,
            final @NotNull ImageProvider imageProvider,
            final @NotNull DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider,
            final @NotNull DockableMenuFactoryProvider dockableMenuFactoryProvider
    ) {
        Objects.requireNonNull(bento);
        this.dockBuilding = bento.dockBuilding();
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
        this.codec = Objects.requireNonNull(codec);
        this.dockableProvider = Objects.requireNonNull(dockableProvider);
        this.imageProvider = Objects.requireNonNull(imageProvider);
        this.dockContainerLeafMenuFactoryProvider = dockContainerLeafMenuFactoryProvider;
        this.dockableMenuFactoryProvider = dockableMenuFactoryProvider;
    }

    @Override
    public DockContainerRootBranch restoreLayout(
            final @NotNull Stage primaryStage
    ) throws BentoStateException {
        try {
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

                                futureState.complete(codec.decode(in));
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

            if (bentoState.getRootBranchStates().isEmpty()) {
                return dockBuilding.root("root-branch");
            }

            // Primary stage root branch is the one with no parent stage state.
            final DockContainerRootBranchState primaryRootBranchState =
                    bentoState.getRootBranchStates().stream()
                            .filter(rb -> rb.getParent().isEmpty())
                            .findFirst()
                            .orElse(bentoState.getRootBranchStates().iterator().next());

            final DockContainerRootBranch primaryRootBranch =
                    restorePrimaryStageRoot(primaryRootBranchState, primaryStage);

            // Restore secondary stages (root branches that have parent stage state)
            for (final DockContainerRootBranchState rootBranchState : bentoState.getRootBranchStates()) {

                if (rootBranchState != primaryRootBranchState) {

                    rootBranchState.getParent().ifPresent(parentStage ->
                            restoreDragDropStage(parentStage, rootBranchState)
                    );
                }
            }

            return primaryRootBranch;
        } catch (final ExecutionException e) {

            throw new BentoStateException(
                    "An error occurred while attempting to read the layout",
                    e
            );
        } catch (final InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new BentoStateException(
                    "Interrupted while attempting to read the layout",
                    e
            );
        }
    }

    private void closeOtherStages(final Stage primaryStage) {

        for (final Stage stage : FxStageUtils.getAllStages()) {

            if (stage != primaryStage) {

                stage.close();
            }
        }
    }

    private @NotNull DockContainerRootBranch restorePrimaryStageRoot(
            final @NotNull DockContainerRootBranchState rootBranchState,
            final @NotNull Stage primaryStage
    ) {
        primaryStage.show();
        return restoreRootBranchContainer(rootBranchState);
    }

    // TODO BENTO-13: Should this use a StageBuilding and, if so, how?
    private void restoreDragDropStage(
            final @NotNull DragDropStageState stageState,
            final @NotNull DockContainerRootBranchState rootBranchState
    ) {
        final DragDropStage stage = new DragDropStage(
                stageState.isAutoClosedWhenEmpty()
        );

        stage.getIcons().addAll(imageProvider.getDefaultStageIcons());
        stageState.getTitle().ifPresent(stage::setTitle);
        stageState.getX().ifPresent(stage::setX);
        stageState.getY().ifPresent(stage::setY);
        stageState.getWidth().ifPresent(stage::setWidth);
        stageState.getHeight().ifPresent(stage::setHeight);

        // Iconified/fullscreen/maximized: apply what exists.
        stageState.isIconified().ifPresent(stage::setIconified);
        stageState.isFullScreen().ifPresent(stage::setFullScreen);
        stageState.isMaximized().ifPresent(stage::setMaximized);

        final Parent rootContainer =
                restoreRootBranchContainer(rootBranchState);
        stage.setScene(new Scene(rootContainer));
        stage.show();
    }

    private @NotNull DockContainerRootBranch restoreRootBranchContainer(
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

        // The root wrapper may contain either one branch or one leaf.
        if (!rootBranchState.getChildDockContainerStates().isEmpty()) {

            final @NotNull DockContainerState childState =
                    rootBranchState.getChildDockContainerStates()
                            .getFirst();

            final DockContainer dockContainer = restoreDockContainer(childState);

            if (dockContainer != null) {

                if(childState instanceof final DockContainerLeafState leafState &&
                        dockContainer instanceof DockContainerLeaf) {

                    leafState.getUncollapsedSizePx().ifPresent(size ->
                            rootBranch.setContainerSizePx(dockContainer, size)
                    );
                }

                rootBranch.addContainer(dockContainer);
            }

        } else if (!rootBranchState.getChildDockableStates().isEmpty()) {

            final @NotNull DockableState dockableState =
                    rootBranchState.getChildDockableStates()
                            .getFirst();

            final Dockable dockable =
                    restoreDockable(dockableState.getIdentifier());

            if (dockable != null) {
                rootBranch.addDockable(dockable);
            }
        }

        return rootBranch;
    }

    private @Nullable DockContainer restoreDockContainer(
            final @NotNull DockContainerState state
    ) {

        switch (state) {
            case final DockContainerBranchState branchState -> {

                return restoreBranch(branchState);
            }
            case final DockContainerLeafState leafState -> {

                return restoreLeaf(leafState);
            }
            default -> {

                logger.warn("Unknown DockContainerState type: {}", state.getClass());
                return null;
            }
        }
    }

    private @NotNull DockContainerBranch restoreBranch(
            final @NotNull DockContainerBranchState branchState
    ) {
        final String id =
                nonEmptyOr(
                        branchState.getIdentifier(),
                        createUniqueIdentifier("branch")
                );

        final DockContainerBranch branch = dockBuilding.branch(id);

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
                    restoreDockContainer(dockContainerState);

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
            final @NotNull DockContainerLeafState state
    ) {
        final String id = nonEmptyOr(
                state.getIdentifier(),
                createUniqueIdentifier("leaf")
        );

        final DockContainerLeaf leaf = dockBuilding.leaf(id);

        state.doPruneWhenEmpty().ifPresent(leaf::setPruneWhenEmpty);

        state.getSide().ifPresent(leaf::setSide);

        state.isResizableWithParent().ifPresent(isResizableWithParent ->
                SplitPane.setResizableWithParent(
                        leaf,
                        isResizableWithParent
                )
        );

        state.isCanSplit().ifPresent(leaf::setCanSplit);

        final String selectedId =
                state.getSelectedDockableIdentifier().orElse(null);

        for (final DockableState dockableState : state.getChildDockableStates()) {

            final String dockableId = dockableState.getIdentifier();

            final Dockable dockable = restoreDockable(dockableId);

            if (dockable != null) {

                leaf.addDockable(dockable);
                if (dockableId.equals(selectedId)) {
                    leaf.selectDockable(dockable);
                }
            } else {

                logger.warn("Dockable with ID '{}' could not be acquired.", dockableId);
            }
        }

        return leaf;
    }

    private @Nullable Dockable restoreDockable(@NotNull String dockableIdentifier) {

        return dockableProvider
                .resolveDockable(dockableIdentifier)
                .orElse(null);
    }

    private static @NotNull String createUniqueIdentifier(final String prefix) {

        final byte[] bytes = new byte[UID_CAPACITY];
        RANDOM.nextBytes(bytes);
        final StringBuilder sb = new StringBuilder(prefix).append("-");
        for (final byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static String nonEmptyOr(
            final String value,
            final String fallback
    ) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
