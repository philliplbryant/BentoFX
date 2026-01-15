/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import javafx.geometry.Side;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static javafx.geometry.Side.TOP;

public class DockContainerLeafState extends DockContainerState {

    private final String selectedDockableStateIdentifier;
    private final Set<@NotNull DockableState> dockableStates;
    private final @NotNull Side side;

    private DockContainerLeafState(
            final @NotNull String identifier,
            final @NotNull Side side,
            final Set<@NotNull DockableState> dockableStates,
            final String selectedDockableStateIdentifier
    ) {
        super(identifier);
        this.side = side;
        this.dockableStates = dockableStates;
        this.selectedDockableStateIdentifier = selectedDockableStateIdentifier;
    }

    public Optional<Side> getSide() {
        return Optional.of(side);
    }

    public Set<@NotNull DockableState> getDockableStates() {
        return Set.copyOf(dockableStates);
    }

    public Optional<String> getSelectedDockableStateIdentifier() {
        return Optional.ofNullable(selectedDockableStateIdentifier);
    }

    public static class DockContainerLeafStateBuilder {

        private final @NotNull String identifier;
        private Side side;
        private String selectedDockableStateIdentifier;
        private final Set<@NotNull DockableState> dockableStates =
                new LinkedHashSet<>();

        public DockContainerLeafStateBuilder(
                final @NotNull String identifier
        ) {
            this.identifier = requireNonNull(identifier);
            this.side = TOP;
        }

        public DockContainerLeafStateBuilder setSide(final Side side) {
            this.side = side;
            return this;
        }

        public DockContainerLeafStateBuilder setSelectedDockableStateIdentifier(
                final String selectedDockableStateIdentifier
        ) {
            this.selectedDockableStateIdentifier =
                    selectedDockableStateIdentifier;
            return this;
        }

        public DockContainerLeafStateBuilder addDockableState(final @NotNull DockableState dockableState) {
            dockableStates.add(dockableState);
            return this;
        }

        public DockContainerLeafState build() {
            return new DockContainerLeafState(
                    identifier,
                    side,
                    dockableStates,
                    selectedDockableStateIdentifier
            );
        }
    }
}
