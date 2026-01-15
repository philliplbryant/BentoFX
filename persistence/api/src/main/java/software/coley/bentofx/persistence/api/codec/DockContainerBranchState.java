/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import javafx.geometry.Orientation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DockContainerBranchState extends DockContainerState {

    private final Orientation orientation;
    private final @NotNull Set<@NotNull DockContainerState> dockContainerStates;
    private final @NotNull Map<@NotNull Integer, @NotNull Double> dividerPositions;

    private DockContainerBranchState(
            final @NotNull String identifier,
            final Orientation orientation,
            final @NotNull Set<@NotNull DockContainerState> dockContainerStates,
            final @NotNull Map<@NotNull Integer, @NotNull Double> dividerPositions
    ) {
        super(identifier);
        this.orientation = orientation;
        this.dockContainerStates = dockContainerStates;
        this.dividerPositions = dividerPositions;
    }

    public Optional<Orientation> getOrientation() {
        return Optional.ofNullable(orientation);
    }

    public @NotNull Set<DockContainerState> getDockContainerStates() {
        return Set.copyOf(dockContainerStates);
    }

    public @NotNull Map<@NotNull Integer, @NotNull Double> getDividerPositions() {
        return Map.copyOf(dividerPositions);
    }

    public static class DockContainerBranchStateBuilder {

        private final @NotNull String identifier;
        private final @NotNull Set<@NotNull DockContainerState> dockContainerStates =
                new LinkedHashSet<>();
        private final @NotNull Map<@NotNull Integer, @NotNull Double> dividerPositions =
                new LinkedHashMap<>();
        private Orientation orientation;

        public DockContainerBranchStateBuilder(final @NotNull String identifier) {
            this.identifier = identifier;
        }

        public @NotNull DockContainerBranchStateBuilder setOrientation(
                final Orientation orientation
        ) {
            this.orientation = orientation;
            return this;
        }

        public @NotNull DockContainerBranchStateBuilder addDockContainerState(
                final @NotNull DockContainerState dockContainerState
        ) {
            this.dockContainerStates.add(requireNonNull(dockContainerState));
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

        public @NotNull DockContainerBranchState build() {
            return new DockContainerBranchState(
                    identifier,
                    orientation,
                    dockContainerStates,
                    dividerPositions
            );
        }
    }
}
