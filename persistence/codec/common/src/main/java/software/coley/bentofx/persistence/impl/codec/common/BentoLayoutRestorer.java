package software.coley.bentofx.persistence.impl.codec.common;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.DockContainer;
import software.coley.bentofx.layout.container.DockContainerBranch;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.persistence.api.DockableResolver;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.codec.BentoState;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.DockContainerBranchState;
import software.coley.bentofx.persistence.api.codec.DockContainerLeafState;
import software.coley.bentofx.persistence.api.codec.DockContainerRootBranchState;
import software.coley.bentofx.persistence.api.codec.DockContainerState;
import software.coley.bentofx.persistence.api.codec.DockableState;
import software.coley.bentofx.persistence.api.codec.DragDropStageState;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Restores JavaFX stage layouts from a persisted {@link BentoState}.
 *
 * <p>This replaces the legacy {@code MementoTreeLayoutRestorer}.</p>
 */
public final class BentoLayoutRestorer implements LayoutRestorer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BentoLayoutRestorer.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int UID_CAPACITY = 8;

    private final @NotNull LayoutStorage layoutStorage;
    private final @NotNull LayoutCodec codec;
    private final @NotNull DockBuilding dockBuilding;
    private final @NotNull DockableResolver dockableResolver;

    public BentoLayoutRestorer(
            final @NotNull LayoutStorage layoutStorage,
            final @NotNull LayoutCodec codec,
            final @NotNull DockBuilding dockBuilding,
            final @NotNull DockableResolver dockableResolver
    ) {
        this.layoutStorage = Objects.requireNonNull(layoutStorage);
        this.codec = Objects.requireNonNull(codec);
        this.dockBuilding = Objects.requireNonNull(dockBuilding);
        this.dockableResolver = Objects.requireNonNull(dockableResolver);
    }

    @Override
    public Parent restoreLayout(
            final Stage primaryStage
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
            final BentoState state = futureState.get();

            if (state.getRootBranchStates().isEmpty()) {
                return new Pane();
            }

            // Primary stage root branch is the one with no parent stage state.
            final DockContainerRootBranchState primaryRoot =
                    state.getRootBranchStates().stream()
                         .filter(rb -> rb.getParent().isEmpty())
                         .findFirst()
                         .orElse(state.getRootBranchStates().iterator().next());

            final Parent primaryRootNode =
                    restorePrimaryStageRoot(primaryRoot, primaryStage);

            // Restore secondary stages (root branches that have parent stage state)
            for (final DockContainerRootBranchState rootBranchState : state.getRootBranchStates()) {

                if (rootBranchState != primaryRoot) {

                    rootBranchState.getParent().ifPresent(parentStage ->
                            restoreDragDropStage(parentStage, rootBranchState)
                    );
                }
            }

            return primaryRootNode;
        } catch (final ExecutionException | InterruptedException ex) {

            throw new BentoStateException(
                    "Interrupted while attempting to read layout",
                    ex
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

    private Parent restorePrimaryStageRoot(
            final @NotNull DockContainerRootBranchState rootBranchState,
            final @NotNull Stage primaryStage
    ) {
        primaryStage.show();
        return restoreRootBranchContainer(rootBranchState);
    }

    private void restoreDragDropStage(
            final @NotNull DragDropStageState stageState,
            final @NotNull DockContainerRootBranchState rootBranchState
    ) {
        final DragDropStage stage = new DragDropStage(
                stageState.isAutoClosedWhenEmpty()
        );

        dockableResolver.getDefaultDragDropStageIcon().ifPresent(icon ->
                stage.getIcons().add(icon)
        );

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

    private Parent restoreRootBranchContainer(
            final @NotNull DockContainerRootBranchState rootBranchState
    ) {
        // The root wrapper may contain either one branch or one leaf.
        if (!rootBranchState.getDockContainerBranchStates().isEmpty()) {

            final DockContainerBranchState branchState =
                    rootBranchState.getDockContainerBranchStates()
                                   .iterator()
                                   .next();
            return restoreBranch(true, branchState);
        } else if (!rootBranchState.getDockContainerLeafStates().isEmpty()) {

            final DockContainerLeafState leafState =
                    rootBranchState.getDockContainerLeafStates()
                                   .iterator()
                                   .next();

            return restoreLeaf(leafState);
        } else {

            return new Pane();
        }
    }

    private DockContainer restoreDockContainer(
            final DockContainerState state
    ) {
        if (state instanceof final DockContainerBranchState branchState) {

            return restoreBranch(false, branchState);
        } else if (state instanceof final DockContainerLeafState leafState) {

            return restoreLeaf(leafState);
        } else {

            LOGGER.warn("Unknown DockContainerState type: {}", state.getClass());
            return null;
        }
    }

    private DockContainerBranch restoreBranch(
            final boolean isRoot,
            final @NotNull DockContainerBranchState branchState
    ) {
        final String id =
                nonEmptyOr(
                        branchState.getIdentifier(),
                        createUniqueIdentifier(isRoot ?
                                "root-branch" :
                                "branch"
                        )
                );

        final DockContainerBranch branch =
                isRoot ?
                        dockBuilding.root(id) :
                        dockBuilding.branch(id);

        // Orientation
        branchState.getOrientation().ifPresent(orientation ->
                branch.orientationProperty().set(orientation)
        );

        // Children
        for (
                final DockContainerState dockContainerState :
                branchState.getDockContainerStates()
        ) {
            final DockContainer container =
                    restoreDockContainer(dockContainerState);

            if (container != null) {

                branch.addContainer(container);
            } else {

                LOGGER.warn(
                        "Attempting to restore null DockContainer from " +
                                "DockContainerState: {}",
                        dockContainerState.getClass()
                );
            }
        }

        // Divider positions (deferred, like legacy impl)
        for (
                final Entry<Integer, Double> positions :
                branchState.getDividerPositions().entrySet()
        ) {
            branch.getDividerPositions()[positions.getKey()] =
                    positions.getValue();
        }

        return branch;
    }

    private DockContainerLeaf restoreLeaf(
            final @NotNull DockContainerLeafState state
    ) {
        final String id = nonEmptyOr(
                state.getIdentifier(),
                createUniqueIdentifier("leaf")
        );

        final DockContainerLeaf leaf = dockBuilding.leaf(id);

        state.getSide().ifPresent(leaf::setSide);

        final String selectedId =
                state.getSelectedDockableStateIdentifier().orElse(null);

        for (final DockableState dockableState : state.getDockableStates()) {
            final String dockableId = dockableState.getIdentifier();

            final Dockable dockable =
                    dockableResolver.resolveDockable(dockableId).orElse(null);
            if (dockable != null) {

                leaf.addDockable(dockable);
                if (dockableId.equals(selectedId)) {
                    leaf.selectDockable(dockable);
                }
            } else {

                LOGGER.warn("Dockable with ID '{}' could not be acquired.", dockableId);
            }
        }

        return leaf;
    }

    private static String createUniqueIdentifier(final String prefix) {

        final byte[] bytes = new byte[UID_CAPACITY];
        RANDOM.nextBytes(bytes);
        final StringBuilder sb = new StringBuilder(prefix).append("-");
        for (final byte b : bytes) {
            sb.append(Integer.toHexString(b & 0xff));
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
