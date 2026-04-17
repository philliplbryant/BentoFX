package software.coley.bentofx.persistence.impl.codec;

import javafx.geometry.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code DockContainerRootBranch}.
 *
 * @author Phil Bryant
 */
public class DockContainerRootBranchState extends DockContainerBranchState {

    private DockContainerRootBranchState(
            final String identifier,
            final @Nullable Boolean pruneWhenEmpty,
            final List<DockableState> childDockableStates,
            final @Nullable Orientation orientation,
            final Map<Integer, Double> dividerPositions,
            final List<DockContainerState> childDockContainerStates
    ) {
        super(
                identifier,
                pruneWhenEmpty,
                childDockableStates,
                orientation,
                dividerPositions,
                childDockContainerStates
        );
    }

    public static class DockContainerRootBranchStateBuilder {

        private final String identifier;
        private final List<DockableState> childDockableStates = new ArrayList<>();
        private @Nullable Boolean pruneWhenEmpty;
        private @Nullable Orientation orientation;
        private final Map<Integer, Double> dividerPositions =
                new LinkedHashMap<>();
        private final List<DockContainerState> childDockContainerStates = new ArrayList<>();

        public DockContainerRootBranchStateBuilder(
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public DockContainerRootBranchStateBuilder addChildDockableState(final DockableState dockableState) {
            this.childDockableStates.add(requireNonNull(dockableState));
            return this;
        }

        public DockContainerRootBranchStateBuilder setPruneWhenEmpty(boolean pruneWhenEmpty) {
            this.pruneWhenEmpty = pruneWhenEmpty;
            return this;
        }

        public DockContainerRootBranchStateBuilder setOrientation(
                final @Nullable Orientation orientation
        ) {
            this.orientation = orientation;
            return this;
        }

        public DockContainerRootBranchStateBuilder addDividerPosition(
                final Integer dividerIndex,
                final Double dividerPosition
        ) {
            dividerPositions.put(
                    requireNonNull(dividerIndex),
                    requireNonNull(dividerPosition)
            );
            return this;
        }

        public DockContainerRootBranchStateBuilder addDockContainerState(
                final DockContainerState dockContainerState
        ) {
            this.childDockContainerStates.add(requireNonNull(dockContainerState));
            return this;
        }

        public DockContainerRootBranchState build() {
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
