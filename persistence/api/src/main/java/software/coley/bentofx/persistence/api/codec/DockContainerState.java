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

    @NotNull
    private final List<DockableState> childDockableStates;
    @Nullable
    private final Boolean pruneWhenEmpty;

    protected DockContainerState(
            final @NotNull String identifier,
            final @Nullable Boolean pruneWhenEmpty,
            final @NotNull List<DockableState> childDockableStates
    ) {
        super(identifier);
        this.childDockableStates = List.of(childDockableStates.toArray(new DockableState[0]));
        this.pruneWhenEmpty = pruneWhenEmpty;
    }

    public @NotNull List<@NotNull DockableState> getChildDockableStates() {
        return childDockableStates;
    }

    public @NotNull Optional<Boolean> doPruneWhenEmpty() {
        return Optional.ofNullable(pruneWhenEmpty);
    }

    public static class DockContainerStateBuilder {

        protected final @NotNull String identifier;
        protected final @NotNull List<DockableState> childDockableStates = new ArrayList<>();
        protected @Nullable Boolean pruneWhenEmpty;

        public DockContainerStateBuilder(
                final @NotNull String identifier
        ) {
            this.identifier = requireNonNull(identifier);
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
                    pruneWhenEmpty,
                    childDockableStates
            );
        }
    }
}
