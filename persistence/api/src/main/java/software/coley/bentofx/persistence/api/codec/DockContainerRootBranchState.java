/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import javafx.geometry.Orientation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the layout state of a {@code DockContainerRootBranch}.
 *
 * @author Phil Bryant
 */
public class DockContainerRootBranchState extends DockContainerBranchState {

    private DockContainerRootBranchState(
            final @NotNull String identifier,
            final @Nullable Boolean pruneWhenEmpty,
            final @NotNull List<DockableState> childDockableStates,
            final @Nullable Orientation orientation,
            final @NotNull Map<@NotNull Integer, @NotNull Double> dividerPositions,
            final @NotNull List<DockContainerState> childDockContainerStates
            ) {
        super(
                identifier,
                null,
                pruneWhenEmpty,
                childDockableStates,
                orientation,
                dividerPositions,
                childDockContainerStates
        );
    }

    @Override
    public Optional<DragDropStageState> getParent() {
        return Optional.empty();
    }

    public static class DockContainerRootBranchStateBuilder extends DockContainerBranchStateBuilder {

        public DockContainerRootBranchStateBuilder(final @NotNull String identifier) {
            super(identifier);
        }

        public @NotNull DockContainerRootBranchState build() {
            return new DockContainerRootBranchState(
                    identifier,
                    pruneWhenEmpty,
                    childDockableStates,
                    orientation,
                    dividerPositions,
                    childDockContainerStates
            );
        }
    }
}
