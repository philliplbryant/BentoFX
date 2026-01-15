/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DockContainerRootBranchState extends IdentifiableState {

    private final DragDropStageState parent;
    private final @NotNull Set<@NotNull DockContainerBranchState> dockContainerBranchStates;
    private final @NotNull Set<@NotNull DockContainerLeafState> dockContainerLeafStates;

    private DockContainerRootBranchState(
            final @NotNull String identifier,
            final DragDropStageState parent,
            final @NotNull Set<@NotNull DockContainerBranchState> dockContainerBranchStates,
            final @NotNull Set<@NotNull DockContainerLeafState> dockContainerLeafStates
    ) {
        super(identifier);
        this.parent = parent;
        this.dockContainerBranchStates = dockContainerBranchStates;
        this.dockContainerLeafStates = dockContainerLeafStates;
    }

    public Optional<DragDropStageState> getParent() {
        return Optional.ofNullable(parent);
    }

    public @NotNull Set<@NotNull DockContainerBranchState> getDockContainerBranchStates() {
        return dockContainerBranchStates;
    }

    public @NotNull Set<@NotNull DockContainerLeafState> getDockContainerLeafStates() {
        return dockContainerLeafStates;
    }

    public static class DockContainerRootBranchStateBuilder {

        private final @NotNull String identifier;
        private final @NotNull Set<@NotNull DockContainerBranchState> dockContainerBranchStates =
                new LinkedHashSet<>();
        private final @NotNull Set<@NotNull DockContainerLeafState> dockContainerLeafStates =
                new LinkedHashSet<>();
        private DragDropStageState parent;

        public DockContainerRootBranchStateBuilder(final @NotNull String identifier) {
            this.identifier = requireNonNull(identifier);
        }

        public @NotNull DockContainerRootBranchStateBuilder setParent(
                final DragDropStageState parent
        ) {
            this.parent = parent;
            return this;
        }

        public @NotNull DockContainerRootBranchStateBuilder addDockContainerBranchState(
                final @NotNull DockContainerBranchState... dockContainerBranchState
        ) {
            this.dockContainerBranchStates.addAll(
                    Set.of(requireNonNull(dockContainerBranchState))
            );
            return this;
        }

        public @NotNull DockContainerRootBranchStateBuilder addDockContainerLeafState(
                final @NotNull DockContainerLeafState... dockContainerLeafState
        ) {
            this.dockContainerLeafStates.addAll(
                    Set.of(requireNonNull(dockContainerLeafState))
            );
            return this;
        }

        public @NotNull DockContainerRootBranchState build() {
            return new DockContainerRootBranchState(
                    identifier,
                    parent,
                    dockContainerBranchStates,
                    dockContainerLeafStates
            );
        }
    }
}
