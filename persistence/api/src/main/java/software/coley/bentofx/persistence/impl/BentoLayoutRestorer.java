package software.coley.bentofx.persistence.impl;

import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
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
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.BentoLayout.BentoLayoutBuilder;
import software.coley.bentofx.persistence.impl.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.impl.codec.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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

    private final LayoutCodec layoutCodec;
    private final LayoutStorage layoutStorage;
    private final BentoProvider bentoProvider;
    private final DockableStateProvider dockableStateProvider;
    private final @Nullable StageIconImageProvider stageIconImageProvider;
    private final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider;

    public BentoLayoutRestorer(
            final LayoutCodec layoutCodec,
            final LayoutStorage layoutStorage,
            final BentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
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
    public DockingLayout restoreLayout(
            final Supplier<DockingLayout> defaultLayoutSupplier
    ) {

        if (!doesLayoutExist()) {
            return defaultLayoutSupplier.get();
        }

        final DockingLayoutBuilder dockingLayoutBuilder =
                new DockingLayoutBuilder();
        try {

            final CompletableFuture<List<BentoState>> futureState =
                    new CompletableFuture<>();

            scheduleService(futureState);

            // Wait for the future to complete
            final List<BentoState> bentoStateList = futureState.get();

            for (final BentoState bentoState : bentoStateList) {

                final String bentoIdentifier = bentoState.getIdentifier();

                final BentoLayoutBuilder bentoLayoutBuilder =
                        new BentoLayoutBuilder(bentoIdentifier);

                // All Bentos are not created equal - it's possible the client
                // application extended the Bento class or otherwise customized
                // its functionality, and used the custom Bento when creating
                // the layout that is being restored. Get the Bento for the
                // bento identifier.
                final Bento bento = bentoProvider.getBento(bentoIdentifier)
                        .orElseGet(() -> {
                                    logger.warn(
                                            "Could not find the Bento with " +
                                                    "identifier {}. Some " +
                                                    "docking features might " +
                                                    "not be available.",
                                            bentoIdentifier
                                    );
                                    return new Bento();
                                }
                        );

                final DockBuilding dockBuilding = bento.dockBuilding();

                for (final DockContainerRootBranchState rootBranchState :
                        bentoState.getRootBranchStates()) {
                    bentoLayoutBuilder.addRootBranch(
                            restoreRootBranchContainer(
                                    dockBuilding,
                                    rootBranchState
                            )
                    );
                }

                // Restore DragDropStages
                for (final DragDropStageState dragDropStageState :
                        bentoState.getDragDropStageStates()) {
                    bentoLayoutBuilder.addDragDropStage(
                            restoreDragDropStage(
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
            final CompletableFuture<List<BentoState>> futureState
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

    private DragDropStage restoreDragDropStage(
            final DockBuilding dockBuilding,
            final DragDropStageState stageState
    ) {

        final DragDropStage dragDropStage = new DragDropStage(
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
            dragDropStage.getIcons().addAll(
                    stageIconImageProvider.getStageIcons()
            );
        }

        // Apply the stage state to the DragDropStage
        stageState.getTitle().ifPresent(dragDropStage::setTitle);
        stageState.getX().ifPresent(dragDropStage::setX);
        stageState.getY().ifPresent(dragDropStage::setY);
        stageState.getWidth().ifPresent(dragDropStage::setWidth);
        stageState.getHeight().ifPresent(dragDropStage::setHeight);
        stageState.getOpacity().ifPresent(dragDropStage::setOpacity);
        stageState.isIconified().ifPresent(dragDropStage::setIconified);
        stageState.isFullScreen().ifPresent(dragDropStage::setFullScreen);
        stageState.isMaximized().ifPresent(dragDropStage::setMaximized);
        stageState.isAlwaysOnTop().ifPresent(dragDropStage::setAlwaysOnTop);
        stageState.isResizable().ifPresent(dragDropStage::setResizable);
        stageState.isFocused()
                .filter(Boolean::booleanValue)
                .ifPresent(ignored -> dragDropStage.requestFocus());
        stageState.getModality().ifPresent(dragDropStage::initModality);

        return dragDropStage;
    }

    private DockContainerRootBranch restoreRootBranchContainer(
            final DockBuilding dockBuilding,
            final DockContainerRootBranchState rootBranchState
    ) {
        final DockContainerRootBranch rootBranch =
                dockBuilding.root(rootBranchState.getIdentifier());

        rootBranchState.doPruneWhenEmpty().ifPresent(
                rootBranch::setPruneWhenEmpty
        );

        rootBranchState.getOrientation().ifPresent(
                rootBranch::setOrientation
        );

        // TODO BENTO-13: Divider positions are not restoring properly
        applyDividerPositions(rootBranchState, rootBranch);
        applyDockContainerStates(rootBranchState, rootBranch);
        applyDockableStates(rootBranchState, rootBranch, dockBuilding);
        //  You can only correctly collapse a leaf if the branch containing the
        //  leaf contains more than one DockContainer, so wait until all
        //  DockContainers have been added to collapse the leaves.
        conditionallyCollapseLeaves(rootBranchState, rootBranch);

        return rootBranch;
    }

    private @Nullable DockContainer restoreDockContainer(
            final DockContainerRootBranch rootBranch,
            final DockContainerState state
    ) {

        switch (state) {
            case final DockContainerBranchState branchState -> {

                return restoreBranch(rootBranch, branchState);
            }
            case final DockContainerLeafState leafState -> {

                return restoreLeaf(rootBranch, leafState);
            }
            default -> {

                logger.warn(
                        "Unknown DockContainerState type: {}",
                        state.getClass()
                );
                return null;
            }
        }
    }

    private DockContainerBranch restoreBranch(
            final DockContainerRootBranch rootBranch,
            final DockContainerBranchState branchState
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
        for (final Map.Entry<Integer, Double> positionEntry :
                branchState.getDividerPositions().entrySet()) {
            branch.setDividerPosition(
                    positionEntry.getKey(),
                    positionEntry.getValue()
            );
        }

        return branch;
    }

    private DockContainerLeaf restoreLeaf(
            final DockContainerRootBranch rootBranch,
            final DockContainerLeafState state
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

        for (final DockableState dockableState :
                state.getChildDockableStates()) {

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

                logger.warn(
                        "Dockable with ID '{}' could not be acquired.",
                        dockableId
                );
            }
        }

        return leaf;
    }

    private @Nullable Dockable restoreDockable(
            final DockBuilding dockBuilding,
            final String dockableIdentifier
    ) {

        final Optional<DockableState> optionalDockableProvider =
                dockableStateProvider.resolveDockableState(dockableIdentifier);

        final DockableState dockableState =
                optionalDockableProvider.orElse(null);

        Dockable dockable;

        if (dockableState != null) {

            dockable = dockBuilding.dockable(dockableIdentifier);

            dockableState.getTitle().ifPresent(
                    dockable::setTitle
            );

            dockableState.getTooltip().ifPresent(tooltipText ->
                    dockable.setTooltip(new Tooltip(tooltipText))
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

    private static void applyDividerPositions(
            final DockContainerRootBranchState rootBranchState,
            final DockContainerRootBranch rootBranch
    ) {
        for (final Map.Entry<Integer, Double> positionEntry :
                rootBranchState.getDividerPositions().entrySet()) {
            rootBranch.setDividerPosition(
                    positionEntry.getKey(),
                    positionEntry.getValue()
            );
        }
    }

    private void applyDockContainerStates(
            final DockContainerRootBranchState rootBranchState,
            final DockContainerRootBranch rootBranch
    ) {
        for (final DockContainerState childState :
                rootBranchState.getChildDockContainerStates()) {

            final DockContainer dockContainer =
                    restoreDockContainer(rootBranch, childState);
            if (dockContainer != null) {
                rootBranch.addContainer(dockContainer);
            }
        }
    }

    private void applyDockableStates(
            final DockContainerRootBranchState rootBranchState,
            final DockContainerRootBranch rootBranch,
            final DockBuilding dockBuilding
    ) {
        for (final DockableState dockableState :
                rootBranchState.getChildDockableStates()) {
            final Dockable dockable =
                    restoreDockable(
                            dockBuilding,
                            dockableState.getIdentifier()
                    );
            if (dockable != null) {
                rootBranch.addDockable(dockable);
            }
        }
    }

    private static void conditionallyCollapseLeaves(
            final DockContainerRootBranchState rootBranchState,
            final DockContainerRootBranch rootBranch) {

        // Map the leaves to their identifiers
        final Map<String, DockContainerLeaf> leaves = new HashMap<>();
        for(final DockContainer container : rootBranch.getChildContainers())
        {
            if(container instanceof final DockContainerLeaf leaf) {
                leaves.put(leaf.getIdentifier(), leaf);
            }
        }

        // For each leaf state,
        for (final DockContainerState childState :
                rootBranchState.getChildDockContainerStates()) {

            if(childState instanceof final DockContainerLeafState leafState) {

                final DockContainerLeaf leaf = leaves.get(leafState.getIdentifier());

                leafState.isCollapsed().ifPresent(isCollapsed ->
                        rootBranch.setContainerCollapsed(leaf, isCollapsed)
                );
            }
        }
    }
}
