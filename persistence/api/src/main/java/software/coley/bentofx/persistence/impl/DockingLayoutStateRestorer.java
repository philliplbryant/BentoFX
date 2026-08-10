package software.coley.bentofx.persistence.impl;

import javafx.application.Platform;
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
import software.coley.bentofx.persistence.api.BentoLayout.BentoLayoutBuilder;
import software.coley.bentofx.persistence.api.DockingLayout;
import software.coley.bentofx.persistence.api.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.DockContainerLeafMenuFactoryProvider;
import software.coley.bentofx.persistence.api.provider.DockableStateProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerState;
import software.coley.bentofx.persistence.api.state.DockableState;
import software.coley.bentofx.persistence.api.state.DragDropStageState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static software.coley.bentofx.persistence.impl.StageUtils.getXInScreenBounds;
import static software.coley.bentofx.persistence.impl.StageUtils.getYInScreenBounds;

/**
 * Restores decoded Bento layout state into live BentoFX runtime objects.
 *
 * @author Phil Bryant
 */
final class DockingLayoutStateRestorer {

    private static final Logger logger =
            LoggerFactory.getLogger(DockingLayoutStateRestorer.class);

    private final BentoProvider bentoProvider;
    private final DockableStateProvider dockableStateProvider;
    private final @Nullable StageIconImageProvider stageIconImageProvider;
    private final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider;

    DockingLayoutStateRestorer(
            final BentoProvider bentoProvider,
            final DockableStateProvider dockableStateProvider,
            final @Nullable StageIconImageProvider stageIconImageProvider,
            final @Nullable DockContainerLeafMenuFactoryProvider dockContainerLeafMenuFactoryProvider
    ) {
        this.bentoProvider = Objects.requireNonNull(bentoProvider);
        this.dockableStateProvider = Objects.requireNonNull(dockableStateProvider);
        this.stageIconImageProvider = stageIconImageProvider;
        this.dockContainerLeafMenuFactoryProvider = dockContainerLeafMenuFactoryProvider;
    }

    /**
     * Restores decoded layout state to JavaFX/BentoFX runtime objects. This
     * method must run on the JavaFX application thread because it creates and
     * mutates JavaFX and BentoFX objects.
     *
     * @param bentoStateList decoded Bento states.
     * @return restored docking layout.
     */
    DockingLayout restoreDockingLayout(
            final List<BentoState> bentoStateList
    ) {
        final DockingLayoutBuilder dockingLayoutBuilder =
                new DockingLayoutBuilder();

        // Restore the BentoLayout, containing root branches and drag/drop
        // stages for each Bento, and add the BentoLayout to the
        // DockingLayout.
        for (final BentoState bentoState : bentoStateList) {

            final String bentoIdentifier = bentoState.getIdentifier();

            final BentoLayoutBuilder bentoLayoutBuilder =
                    new BentoLayoutBuilder(bentoIdentifier);

            // All Bentos are not created equal - it's possible the client
            // application extended the Bento class or otherwise customized
            // its functionality, and used the custom Bento when creating
            // the layout that is being restored. Use the BentoProvider to
            // get the Bento for the bento identifier.
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

            // Restore each DockContainerRootBranch and add it to the
            // BentoLayout.
            for (final DockContainerRootBranchState rootBranchState :
                    bentoState.getRootBranchStates()) {
                bentoLayoutBuilder.addRootBranch(
                        restoreRootBranchContainer(
                                dockBuilding,
                                rootBranchState
                        )
                );
            }

            // Restore each DragDropStage and add it to the BentoLayout
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
    }

    /**
     * Creates a {@link DragDropStage}, restore its
     * {@link DockContainerRootBranch}, adds the {@link DockContainerRootBranch}
     * to the {@link DragDropStage}, and applies the {@link DragDropStageState}
     * to the {@link DragDropStage}.
     *
     * @param dockBuilding the {@link DockBuilding} to use to create
     *                     {@link DockContainer}s and {@link Dockable}s in the {@link DragDropStage}.
     * @param stageState   the {@link DragDropStageState} defining the persisted
     *                     layout for the {@link DragDropStage}.
     * @return the restored {@link DragDropStage}.
     */
    private DragDropStage restoreDragDropStage(
            final DockBuilding dockBuilding,
            final DragDropStageState stageState
    ) {

        final DragDropStage dragDropStage = new DragDropStage(
                stageState.isAutoClosedWhenEmpty()
        );

        // Create the DockContainerRootBranch and add it to the DragDropStage
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

        // Restore the DragDropStage's icons
        if (stageIconImageProvider != null) {
            dragDropStage.getIcons().addAll(
                    stageIconImageProvider.getStageIcons()
            );
        }

        // Apply the stage state to the DragDropStage
        stageState.getTitle().ifPresent(dragDropStage::setTitle);
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

        stageState.getX().ifPresent(stageX -> {
                    final double xInScreenBounds =
                            getXInScreenBounds(dragDropStage, stageX);
                    dragDropStage.setX(xInScreenBounds);
                }
        );

        stageState.getY().ifPresent(stageY -> {
                    final double yInScreenBounds =
                            getYInScreenBounds(dragDropStage, stageY);
                    dragDropStage.setY(yInScreenBounds);
                }
        );

        return dragDropStage;
    }

    /**
     * Creates a {@link DockContainerRootBranch}, restore its
     * {@link DockContainer}s and {@link Dockable}s, and applies the
     * {@link DockContainerRootBranchState} to the
     * {@link DockContainerRootBranch}.
     *
     * @param dockBuilding    the {@link DockBuilding} to use to create
     *                        {@link DockContainer}s and {@link Dockable}s in the
     *                        {@link DockContainerRootBranch}.
     * @param rootBranchState the {@link DockContainerRootBranchState}
     *                        defining the persisted layout for the
     *                        {@link DockContainerRootBranch}.
     * @return the restored {@link DockContainerRootBranch}.
     */
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

        restoreAndAddChildDockables(dockBuilding, rootBranchState, rootBranch);
        restoreChildDockContainers(dockBuilding, rootBranchState, rootBranch);
        applyDividerPositions(
                rootBranchState.getDividerPositions().entrySet(),
                rootBranch
        );
        conditionallyCollapseLeaves(
                getLeaves(rootBranch),
                getLeafStates(rootBranchState),
                rootBranch
        );

        return rootBranch;
    }

    /**
     * Restores all {@link DockContainer} children for
     * {@link DockContainerRootBranch} and adds each {@link DockContainer} to
     * the {@link DockContainerRootBranch}.
     *
     * @param dockBuilding    the {@link DockBuilding} to use to create
     *                        {@link DockContainer}s and {@link Dockable}s in the {@link DragDropStage}.
     * @param rootBranchState the {@link DockContainerRootBranchState}
     *                        containing the {@link DockContainerState} of the
     *                        {@link DockContainerRootBranch}'s children.
     * @param rootBranch      the {@link DockContainerRootBranch} whose children are
     *                        to be restored.
     */
    private void restoreChildDockContainers(
            final DockBuilding dockBuilding,
            final DockContainerRootBranchState rootBranchState,
            final DockContainerRootBranch rootBranch
    ) {
        for (final DockContainerState childState :
                rootBranchState.getChildDockContainerStates()) {

            final DockContainer dockContainer =
                    restoreDockContainer(dockBuilding, childState);
            if (dockContainer != null) {
                rootBranch.addContainer(dockContainer);
            }
        }
    }

    /**
     * Restores a {@link DockContainer} from a {@link DockContainerBranchState
     * or {@link DockContainerLeafState}.
     * @param dockBuilding       the {@link DockBuilding} used to create the restored
     *                           {@link DockContainer}.
     * @param dockContainerState the {@link DockContainerState} defining the
     *                           persisted layout for the {@link DockContainer}.
     * @return the restored {@link DockContainer}, {@code null} if the
     * {@link DockContainer} could not be restored.
     */
    private @Nullable DockContainer restoreDockContainer(
            final DockBuilding dockBuilding,
            final DockContainerState dockContainerState
    ) {

        return switch (dockContainerState) {
            case final DockContainerBranchState branchState -> restoreBranch(dockBuilding, branchState);
            case final DockContainerLeafState leafState -> restoreLeaf(dockBuilding, leafState);
            default -> {
                logger.warn(
                        "Unknown DockContainerState type: {}",
                        dockContainerState.getClass()
                );
                yield null;
            }
        };
    }

    /**
     * Restores a {@link DockContainerBranch} from a
     * {@link DockContainerBranchState}.
     *
     * @param dockBuilding the {@link DockBuilding} used to create the restored
     *                     {@link DockContainerBranch}.
     * @param branchState  the {@link DockContainerBranchState} defining the
     *                     persisted layout for the {@link DockContainerBranch}.
     * @return the restored {@link DockContainerBranch}.
     */
    private DockContainerBranch restoreBranch(
            final DockBuilding dockBuilding,
            final DockContainerBranchState branchState
    ) {
        final DockContainerBranch branch =
                dockBuilding.branch(branchState.getIdentifier());

        branchState.doPruneWhenEmpty().ifPresent(
                branch::setPruneWhenEmpty
        );

        branchState.getOrientation().ifPresent(orientation ->
                branch.orientationProperty().set(orientation)
        );

        // Restore child DockContainers and add them to their parent branch
        for (final DockContainerState dockContainerState :
                branchState.getChildDockContainerStates()) {

            final DockContainer container =
                    restoreDockContainer(dockBuilding, dockContainerState);

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

        applyDividerPositions(
                branchState.getDividerPositions().entrySet(),
                branch
        );

        // Mirrors restoreRootBranchContainer. Both are needed: getLeaves and
        // getLeafStates only look at direct children, so each branch has to
        // collapse its own leaves. Every branch below the root is built here, so
        // the two calls together cover every leaf at every depth. Without this
        // one, a layout deeper than root-to-leaf silently lost the collapsed
        // state of every leaf it contained.
        conditionallyCollapseLeaves(
                getLeaves(branch),
                getLeafStates(branchState),
                branch
        );

        return branch;
    }

    /**
     * Restores a {@link DockContainerLeaf} from a {@link DockContainerLeafState}.
     *
     * @param dockBuilding the {@link DockBuilding} used to create the restored
     *                     {@link DockContainerLeaf}.
     * @param leafState    the {@link DockContainerLeafState} defining the
     *                     persisted layout for the {@link DockContainerLeaf}.
     * @return the restored {@link DockContainerLeaf}.
     */
    private DockContainerLeaf restoreLeaf(
            final DockBuilding dockBuilding,
            final DockContainerLeafState leafState
    ) {
        // Create the leaf and restore its state
        final DockContainerLeaf leaf =
                dockBuilding.leaf(leafState.getIdentifier());
        leafState.doPruneWhenEmpty().ifPresent(leaf::setPruneWhenEmpty);
        leafState.getSide().ifPresent(leaf::setSide);
        leafState.isCanSplit().ifPresent(leaf::setCanSplit);
        leafState.isResizableWithParent().ifPresent(isResizableWithParent ->
                SplitPane.setResizableWithParent(
                        leaf,
                        isResizableWithParent
                )
        );

        // Set the menu factory
        if (dockContainerLeafMenuFactoryProvider != null) {
            dockContainerLeafMenuFactoryProvider.getDockContainerLeafMenuFactory(
                    leaf.getIdentifier()
            ).ifPresent(
                    leaf::setMenuFactory
            );
        }

        // The identifier of the selected Dockable
        final String selectedId =
                leafState.getSelectedDockableIdentifier().orElse(null);

        // Restore Dockables and, if one is selected, select it
        for (final DockableState dockableState :
                leafState.getChildDockableStates()) {

            final String dockableId = dockableState.getIdentifier();
            final Dockable dockable = restoreDockable(dockBuilding, dockableId);

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

    /**
     * Uses the {@link DockableStateProvider} to resolve the
     * {@link DockableState} and then applies that {@link DockableState} to newly
     * created {@link Dockable}.
     *
     * @param dockBuilding       the {@link DockBuilding} used to create the restored
     *                           {@link Dockable}.
     * @param dockableIdentifier the identifier of the {@link Dockable} to be
     *                           restored.
     * @return the restored {@link Dockable} if its {@link DockableState} is
     * provided by the {@link DockableStateProvider}; otherwise, returns
     * {@code null}.
     */
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

            dockableState.getTooltipText().ifPresent(tooltipText ->
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

    /**
     * Restores all {@link Dockable} children for a
     * {@link DockContainerRootBranch} and adds each {@link Dockable} to
     * the {@link DockContainerRootBranch}.
     *
     * @param dockBuilding    the {@link DockBuilding} to use to create
     *                        {@link DockContainer}s and {@link Dockable}s in the {@link DragDropStage}.
     * @param rootBranchState the {@link DockContainerRootBranchState}
     *                        containing the {@link DockContainerState} of the
     *                        {@link DockContainerRootBranch}'s children.
     * @param rootBranch      the {@link DockContainerRootBranch} whose children are
     *                        to be restored.
     */
    private void restoreAndAddChildDockables(
            final DockBuilding dockBuilding,
            final DockContainerRootBranchState rootBranchState,
            final DockContainerRootBranch rootBranch
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

    /**
     * Applies divider positions to a branch, deferred until after the initial
     * layout pass.
     *
     * @param dividerPositions the diver positions to be applied.
     * @param branch           the {@link DockContainerBranch} containing the
     *                         {@code Divider}s.
     */
    private static void applyDividerPositions(
            Collection<Map.Entry<Integer, Double>> dividerPositions,
            final DockContainerBranch branch
    ) {
        for (final Map.Entry<Integer, Double> positionEntry : dividerPositions) {
            // Defer setting the divider position until after the initial layout pass
            Platform.runLater(() ->
                    branch.setDividerPosition(
                            positionEntry.getKey(),
                            positionEntry.getValue()
                    )
            );
        }
    }

    /**
     * Maps {@link DockContainerLeaf} to their identifiers.
     *
     * @return an unmodifiable a {@link Map} whose keys are
     * {@link DockContainerLeaf#getIdentifier} and whose values are
     * {@link DockContainerLeaf}
     */
    private static Map<String, DockContainerLeaf> getLeaves(
            final DockContainerBranch branch
    ) {
        // Map the leaves to their identifiers
        final Map<String, DockContainerLeaf> leaves = new HashMap<>();
        for (final DockContainer container : branch.getChildContainers()) {
            if (container instanceof final DockContainerLeaf leaf) {
                leaves.put(leaf.getIdentifier(), leaf);
            }
        }
        return Map.copyOf(leaves);
    }

    /**
     * Returns an unmodifiable collection of all {@link DockContainerLeafState}.
     * found in a {@link DockContainerBranchState}
     *
     * @param branchState the {@link DockContainerBranchState} whose
     *                    {@link DockContainerLeafState} are to be found.
     * @return an unmodifiable collection of all {@link DockContainerLeafState}.
     */
    private static Collection<DockContainerLeafState> getLeafStates(
            final DockContainerBranchState branchState
    ) {
        List<DockContainerLeafState> leafStates = new ArrayList<>();
        for (final DockContainerState childState :
                branchState.getChildDockContainerStates()) {
            if (childState instanceof final DockContainerLeafState leafState) {
                leafStates.add(leafState);
            }
        }
        return List.copyOf(leafStates);
    }

    /**
     * Conditionally collapses {@link DockContainerLeaf} instances.
     * This method should not be called until all {@link DockContainerLeaf} have
     * been added to the {@link DockContainerBranch} because a
     * {@link DockContainerLeaf} can only be collapsed if the
     * {@link DockContainerBranch} containing it contains more than one
     * {@link DockContainer}.
     *
     * @param leaves     the {@link DockContainerLeaf} to be conditionally collapsed.
     * @param leafStates the {@link DockContainerLeafState}
     * @param branch     the {@link DockContainerBranch} containing the
     *                   {@link DockContainerLeaf}.
     */
    private static void conditionallyCollapseLeaves(
            final Map<String, DockContainerLeaf> leaves,
            final Collection<DockContainerLeafState> leafStates,
            final DockContainerBranch branch
    ) {
        for (final DockContainerLeafState leafState : leafStates) {
            final DockContainerLeaf leaf = leaves.get(leafState.getIdentifier());
            if (leaf != null) {
                leafState.isCollapsed().ifPresent(isCollapsed ->
                        branch.setContainerCollapsed(leaf, isCollapsed)
                );
            }
        }
    }
}
