/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code DockContainer}.
 *
 * @author Phil Bryant
 */
public class DockContainerState extends IdentifiableState {

    @Nullable private final DragDropStageState parent;
    @NotNull private final List<DockableState> childDockableStates;
    @Nullable private final Boolean pruneWhenEmpty;

    protected DockContainerState(
            final @NotNull String identifier,
            final @Nullable DragDropStageState parent,
            final @Nullable Boolean pruneWhenEmpty,
            final @NotNull List<DockableState> childDockableStates
    ) {
        super(identifier);
        this.parent = parent;
        this.childDockableStates = List.of(childDockableStates.toArray(new DockableState[0]));
        this.pruneWhenEmpty = pruneWhenEmpty;
    }

    public Optional<DragDropStageState> getParent() {
        return Optional.ofNullable(parent);
    }

    public @NotNull List<@NotNull DockableState> getChildDockableStates() {
        return childDockableStates;
    }

    @NotNull private Optional<Boolean> doPruneWhenEmpty() {
        return Optional.ofNullable(pruneWhenEmpty);
    }

    public static class DockContainerStateBuilder {

        protected @NotNull final String identifier;
        protected @Nullable DragDropStageState parent;
        protected @NotNull final List<DockableState> childDockableStates = new ArrayList<>();
        protected @Nullable Boolean pruneWhenEmpty;

        public DockContainerStateBuilder(
                final @NotNull String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public @NotNull DockContainerStateBuilder setParent(
                final @Nullable DragDropStageState parent
        ) {
            this.parent = parent;
            return this;
        }

        public @NotNull DockContainerStateBuilder addChildDockableState(final @NotNull DockableState dockableState) {
            this.childDockableStates.add(requireNonNull(dockableState));
            return this;
        }

        public @NotNull DockContainerStateBuilder setPruneWhenEmpty(boolean pruneWhenEmpty) {
            this.pruneWhenEmpty = pruneWhenEmpty;
            return this;
        }

        public @NotNull DockContainerState build() {
            return new DockContainerState(
                    identifier,
                    parent,
                    pruneWhenEmpty,
                    childDockableStates
            );
        }
    }
}
