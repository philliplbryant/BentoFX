package software.coley.bentofx.persistence.core.impl;

import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.core.api.state.BentoState;
import software.coley.bentofx.persistence.core.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DockContainerRootBranchState;
import software.coley.bentofx.persistence.core.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DockableState;
import software.coley.bentofx.persistence.core.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.core.api.state.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.core.impl.provider.DefaultBentoProvider;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for what a restore that fails part way through leaves behind.
 *
 * <p>A drag/drop stage's root branch registers with its {@link Bento} as soon
 * as the stage has a scene, which happens before the rest of the layout is
 * built. A restore that then fails used to leave those roots registered but
 * never shown, and the next save captured them as extra root branches.</p>
 *
 * @author Phil Bryant
 */
@ExtendWith(ApplicationExtension.class)
class FailedRestoreRollbackITG {

    private static final String BENTO_ID = "bento-failed-restore";
    private static final String GOOD_LEAF_ID = "leaf-restores";
    private static final String BAD_LEAF_ID = "leaf-callback-throws";

    @Test
    void aRestoreThatFailsLeavesNoRootsRegistered(final FxRobot robot) {
        final Bento bento = new Bento(BENTO_ID);
        final AtomicReference<RuntimeException> failure = new AtomicReference<>();
        final AtomicInteger rootsAfterFailure = new AtomicInteger(-1);

        robot.interact(() -> {
            final DockingLayoutStateRestorer restorer = new DockingLayoutStateRestorer(
                    new DefaultBentoProvider(bento),
                    id -> Optional.of(createDockableState(id)),
                    null,
                    leafIdentifier -> {
                        if (BAD_LEAF_ID.equals(leafIdentifier)) {
                            throw new IllegalStateException("application callback failed");
                        }
                        return Optional.empty();
                    }
            );

            try {
                restorer.restoreDockingLayout(List.of(createBentoState()));
            } catch (final RuntimeException e) {
                failure.set(e);
            }

            rootsAfterFailure.set(bento.getRootContainers().size());
        });

        assertThat(failure.get())
                .describedAs("failure from the application callback")
                .isInstanceOf(IllegalStateException.class);
        assertThat(rootsAfterFailure.get())
                .describedAs("root branches still registered after the failed restore")
                .isZero();
    }

    /**
     * {@return a Bento with two drag/drop stages, the second holding the leaf
     * whose menu factory callback throws.}
     */
    private static BentoState createBentoState() {
        return new BentoStateBuilder(BENTO_ID)
                .addDragDropStageState(new DragDropStageStateBuilder(false)
                        .setDockContainerRootBranchState(createRoot("root-first", GOOD_LEAF_ID))
                        .build())
                .addDragDropStageState(new DragDropStageStateBuilder(false)
                        .setDockContainerRootBranchState(createRoot("root-second", BAD_LEAF_ID))
                        .build())
                .build();
    }

    private static DockContainerRootBranchState createRoot(
            final String rootIdentifier,
            final String leafIdentifier
    ) {
        final DockContainerLeafStateBuilder leaf =
                new DockContainerLeafStateBuilder(leafIdentifier);
        leaf.addChildDockableState(createDockableState(leafIdentifier + "-dockable"));

        final DockContainerRootBranchStateBuilder root =
                new DockContainerRootBranchStateBuilder(rootIdentifier);
        root.addDockContainerState(leaf.build());
        return root.build();
    }

    private static DockableState createDockableState(final String identifier) {
        return new DockableStateBuilder(identifier)
                .setTitle(identifier)
                .setDockableNode(new Label(identifier))
                .build();
    }
}
