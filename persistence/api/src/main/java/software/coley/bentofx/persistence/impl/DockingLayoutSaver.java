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
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.codec.*;
import software.coley.bentofx.persistence.impl.codec.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DragDropStageState.DragDropStageStateBuilder;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static software.coley.bentofx.persistence.impl.StageUtils.getAllStages;

/**
 * Saves the layout of all JavaFX {@link Stage}s into a {@link BentoState} and
 * persists it via a {@link LayoutCodec}.
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

                        saveDragDropStage(
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
                        getRootBranchState(rootBranch)
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

    private void saveDragDropStage(
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
                    getRootBranchState(rootBranch);

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

    private DockContainerState saveDockContainer(
            final DockContainer dockContainer
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
                return new DockContainerLeafStateBuilder("leaf-empty")
                        .build();
            }
        }
    }

    private DockContainerRootBranchState getRootBranchState(
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
            builder.addDockContainerState(saveDockContainer(dockContainer));
        }

        return builder.build();
    }

    private DockContainerBranchState getBranchState(
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

        setCommonDockContainerBranchProperties(builder, branch);

        for (final Dockable dockable : branch.getDockables()) {
            builder.addChildDockableState(saveDockable(dockable));
        }

        return builder.build();
    }

    private void setCommonDockContainerBranchProperties(
            final DockContainerBranchStateBuilder builder,
            final DockContainerBranch branch
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

    private DockContainerLeafState saveLeaf(
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
                        saveDockable(dockable)
                );
            } catch (final Exception ex) {

                logger.error("Failed to persist dockable in leaf {}", id, ex);
            }
        }

        return leafStateBuilder.build();
    }

    private DockableState saveDockable(final Dockable dockable) {
        return new DockableStateBuilder(dockable.getIdentifier())
                .build();
    }

    private static String nonEmptyOr(
            final String value,
            final String fallback
    ) {
        return !value.isBlank() ? value : fallback;
    }
}
