/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import javafx.geometry.Orientation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code DockContainerBranch}.
 *
 * @author Phil Bryant
 */
public class DockContainerBranchState extends DockContainerState {

    private final @Nullable Orientation orientation;
    private final @NotNull Map<@NotNull Integer, @NotNull Double> dividerPositions;
    private final @NotNull List<DockContainerState> childDockContainerStates;

    protected DockContainerBranchState(
            final @NotNull String identifier,
            final @Nullable Boolean pruneWhenEmpty,
            final @NotNull List<DockableState> childDockableStates,
            final @Nullable Orientation orientation,
            final @NotNull Map<@NotNull Integer, @NotNull Double> dividerPositions,
            final @NotNull List<DockContainerState> childDockContainerStates
    ) {
        super(
                identifier,
                pruneWhenEmpty,
                childDockableStates
        );
        this.orientation = orientation;
        this.dividerPositions = dividerPositions;
        this.childDockContainerStates =
                List.of(
                        childDockContainerStates.toArray(
                                new DockContainerState[0]
                        )
                );
    }

    public Optional<Orientation> getOrientation() {
        return Optional.ofNullable(orientation);
    }

    public @NotNull Map<@NotNull Integer, @NotNull Double> getDividerPositions() {
        return Map.copyOf(dividerPositions);
    }

    public @NotNull List<@NotNull DockContainerState> getChildDockContainerStates() {
        return childDockContainerStates;
    }

    public static class DockContainerBranchStateBuilder extends DockContainerStateBuilder {

        protected @Nullable Orientation orientation;
        protected final @NotNull Map<@NotNull Integer, @NotNull Double> dividerPositions =
                new LinkedHashMap<>();
        protected @NotNull List<DockContainerState> childDockContainerStates = new ArrayList<>();

        public DockContainerBranchStateBuilder(final @NotNull String identifier) {

            super(identifier);
        }

        public @NotNull DockContainerBranchStateBuilder setOrientation(
                final @Nullable Orientation orientation
        ) {
            this.orientation = orientation;
            return this;
        }

        public @NotNull DockContainerBranchStateBuilder addDividerPosition(
                final @NotNull Integer dividerIndex,
                final @NotNull Double dividerPosition
        ) {
            dividerPositions.put(
                    requireNonNull(dividerIndex),
                    requireNonNull(dividerPosition)
            );
            return this;
        }

        public @NotNull DockContainerBranchStateBuilder addDockContainerState(
                final @NotNull DockContainerState dockContainerState
        ) {
            this.childDockContainerStates.add(requireNonNull(dockContainerState));
            return this;
        }

        @Override
        public @NotNull DockContainerBranchState build() {
            return new DockContainerBranchState(
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
