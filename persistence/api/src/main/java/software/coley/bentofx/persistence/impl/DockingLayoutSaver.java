package software.coley.bentofx.persistence.impl;

import javafx.scene.Parent;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.state.*;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.state.DragDropStageState.DragDropStageStateBuilder;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static software.coley.bentofx.persistence.impl.StageUtils.getAllStages;

/**
 * Automatically saves the layout of all {@link Bento}s into {@link BentoState},
 * encodes them via a {@link LayoutCodec}, and persists them via a
 * {@link LayoutStorage}.
 *
 * @author Phil Bryant
 */
public class DockingLayoutSaver extends AbstractAutoCloseableLayoutSaver {

    private static final Logger logger =
            LoggerFactory.getLogger(DockingLayoutSaver.class);

    private final LayoutStorage layoutStorage;

    private final LayoutCodec layoutCodec;

    /**
     * Creates a {@link DockingLayoutSaver}
     *
     * @param layoutCodec   the {@link LayoutCodec} to use to encode the persisted
     *                      layout.
     * @param layoutStorage the {@link LayoutStorage} to use to write the
     *                      persisted layout.
     * @param bentoProvider the {@link BentoProvider} to use to get {@link Bento}
     *                      instances from their identifiers.
     */
    public DockingLayoutSaver(
            final LayoutCodec layoutCodec,
            final LayoutStorage layoutStorage,
            final BentoProvider bentoProvider
    ) {
        super(bentoProvider);
        this.layoutCodec = Objects.requireNonNull(layoutCodec);
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
    }

    @Override
    public void saveLayout() throws BentoStateException {

        final List<BentoState> bentoStateList =
                new ArrayList<>();

        for (Bento bento : bentoProvider.getAllBentos()) {

            final BentoStateBuilder bentoStateBuilder =
                    new BentoStateBuilder(bento.getIdentifier());

            //  bento.getRootContainers() returns a list of all root branches,
            //  including those in drag drop stages. Create a list of all root
            //  branches, remove the DragDropStage root branches from that list,
            //  and only save the remaining root branches.
            final List<DockContainerRootBranch> nonDragDropStageRootBranches
                    = new ArrayList<>(bento.getRootContainers());

            for (final Stage stage : getAllStages()) {

                if (stage instanceof final DragDropStage dragDropStage) {

                    final Parent parent =
                            dragDropStage.getScene().getRoot();

                    if (parent instanceof final DockContainerRootBranch rootBranch &&
                            bento.matchesIdentity(rootBranch.getBento())) {

                        // Remove the DragDropStage root branch from the
                        // list of all root branches
                        nonDragDropStageRootBranches.remove(rootBranch);

                        buildAndAddDragDropStage(
                                dragDropStage,
                                bentoStateBuilder
                        );
                    }
                }
            }

            // Save the remaining root branches
            for (final DockContainerRootBranch rootBranch :
                    nonDragDropStageRootBranches) {
                bentoStateBuilder.addRootBranchState(
                        buildRootBranchState(rootBranch)
                );
            }

            bentoStateList.add(bentoStateBuilder.build());
        }

        try (final OutputStream out = layoutStorage.openOutputStream()) {

            layoutCodec.encode(bentoStateList, out);
        } catch (final Exception ex) {

            throw new BentoStateException("Failed to encode BentoState", ex);
        }
    }

    /**
     * Saves the state of a {@link DragDropStage} to a {@link BentoStateBuilder}.
     *
     * @param dragDropStage     the {@link DragDropStage} whose state is to be saved.
     * @param bentoStateBuilder the {@link BentoStateBuilder} to which the
     *                          {@link DragDropStage} state is to be saved.
     */
    private void buildAndAddDragDropStage(
            DragDropStage dragDropStage,
            BentoStateBuilder bentoStateBuilder
    ) {
        // A DragDropStage can only have one rootBranch
        final DockContainerRootBranch rootBranch =
                getDockContainerRootBranch(dragDropStage);

        if (rootBranch == null) {
            logger.debug("Ignoring unknown root branch {}", dragDropStage);
        } else {
            final DockContainerRootBranchState rootBranchState =
                    buildRootBranchState(rootBranch);

            bentoStateBuilder.addDragDropStageState(
                    new DragDropStageStateBuilder(
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

    /**
     * Gets the {@link DockContainerRootBranch} for a {@link DragDropStage}
     * ({@link DragDropStage} can only have one {@link DockContainerRootBranch}.
     * @param stage the {@link DragDropStage} whose {@link DockContainerRootBranch}
     *              is to be found.
     * @return the {@link DockContainerRootBranch} for the specified
     * {@link DragDropStage}.
     */
    private @Nullable DockContainerRootBranch getDockContainerRootBranch(
            final DragDropStage stage
    ) {
        final Parent parent = stage.getScene().getRoot();
        if (parent instanceof final DockContainerRootBranch rootBranch) {
            return rootBranch;
        } else {
            logger.debug("Ignoring unknown parent {}", parent);
            return null;
        }
    }

    /**
     * Builds a {@link DockContainerRootBranchState} for a
     * {@link DockContainerRootBranch}.
     * @param branch the {@link DockContainerRootBranch} whose
     * {@link DockContainerRootBranchState} is to be built.
     * @return the {@link DockContainerRootBranchState}.
     */
    private DockContainerRootBranchState buildRootBranchState(
            final DockContainerRootBranch branch
    ) {
        final DockContainerRootBranchStateBuilder builder =
                new DockContainerRootBranchStateBuilder(
                        branch.getIdentifier()
                );

        builder.setPruneWhenEmpty(branch.doPruneWhenEmpty());

        builder.setOrientation(branch.orientationProperty().get());

        // Divider positions (supports multiple)
        final double[] positions = branch.getDividerPositions();

        for (int i = 0; i < positions.length; i++) {

            builder.addDividerPosition(i, positions[i]);
        }

        for (final DockContainer dockContainer : branch.getChildContainers()) {
            builder.addDockContainerState(buildDockContainerState(dockContainer));
        }

        return builder.build();
    }

    /**
     * Builds a {@link DockContainerState} for a {@link DockContainer}. If the
     * {@link DockContainer} is not a branch or leaf, which should not happen,
     * builds an empty leaf to keep the state valid.
     * @param dockContainer the {@link DockContainer} whose
     * {@link DockContainerState} is to be built.
     * @return the {@link DockContainerState}.
     */
    private DockContainerState buildDockContainerState(
            final DockContainer dockContainer
    ) {

        switch (dockContainer) {

            case final DockContainerBranch branch -> {

                return buildBranchState(branch);
            }

            case final DockContainerLeaf leaf -> {
                return buildLeafState(leaf);
            }

            default -> {
                logger.warn("Unsupported node type: {}", dockContainer);
                // Fallback: empty leaf to keep the state valid.
                return new DockContainerLeafStateBuilder("leaf-empty")
                        .build();
            }
        }
    }

    /**
     * Builds a {@link DockContainerBranchState} for a {@link DockContainerBranch}.
     * @param branch the {@link DockContainerBranch} whose
     * {@link DockContainerBranchState} is to be built.
     * @return the {@link DockContainerBranchState}.
     */
    private DockContainerBranchState buildBranchState(
            final DockContainerBranch branch
    ) {
        final String id = nonEmptyOr(
                branch.getIdentifier(),
                "branch-" + System.identityHashCode(branch)
        );

        final DockContainerBranchStateBuilder builder =
                new DockContainerBranchStateBuilder(
                        id
                );

        builder.setPruneWhenEmpty(branch.doPruneWhenEmpty());

        builder.setOrientation(branch.orientationProperty().get());

        // Divider positions (supports multiple)
        final double[] positions = branch.getDividerPositions();

        for (int i = 0; i < positions.length; i++) {

            builder.addDividerPosition(i, positions[i]);
        }

        for (final DockContainer dockContainer : branch.getChildContainers()) {
            builder.addDockContainerState(buildDockContainerState(dockContainer));
        }

        for (final Dockable dockable : branch.getDockables()) {
            builder.addChildDockableState(buildDockable(dockable));
        }

        return builder.build();
    }

    /**
     * Builds a {@link DockContainerLeafState} for a {@link DockContainerLeaf}.
     * @param leaf the {@link DockContainerLeaf} whose
     * {@link DockContainerLeafState} is to be built.
     * @return the {@link DockContainerLeafState}.
     */
    private DockContainerLeafState buildLeafState(
            final DockContainerLeaf leaf
    ) {

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

            leafStateBuilder.setSelectedDockableStateIdentifier(
                    selected.getIdentifier()
            );
        }

        // Dockables
        for (final Dockable dockable : leaf.getDockables()) {

            try {
                leafStateBuilder.addChildDockableState(
                        buildDockable(dockable)
                );
            } catch (final Exception ex) {

                logger.error("Failed to persist dockable in leaf {}", id, ex);
            }
        }

        return leafStateBuilder.build();
    }

    /**
     * Builds a {@link DockableState} for a {@link Dockable}.
     * @param dockable the {@link Dockable} whose {@link DockableState} is to
     * be built.
     * @return the {@link DockableState}.
     */
    private DockableState buildDockable(final Dockable dockable) {
        return new DockableStateBuilder(dockable.getIdentifier())
                .build();
    }

    /**
     * Returns the non-blank value of a {@link String}.
     * @param value the {@link String} whose non-blankness is to be returned.
     * @param fallback the value to return when the {@link String} is blank.
     * @return the non-blank value of a {@link String}.
     */
    private static String nonEmptyOr(
            final String value,
            final String fallback
    ) {
        return !value.isBlank() ? value : fallback;
    }
}
