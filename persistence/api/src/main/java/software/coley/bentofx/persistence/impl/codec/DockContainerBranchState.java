package software.coley.bentofx.persistence.impl.codec;

import javafx.geometry.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code DockContainerBranch}.
 *
 * @author Phil Bryant
 */
public class DockContainerBranchState extends DockContainerState {

    private final @Nullable Orientation orientation;
    private final Map<Integer, Double> dividerPositions;
    private final List<DockContainerState> childDockContainerStates;

    protected DockContainerBranchState(
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

    public Map<Integer, Double> getDividerPositions() {
        return Map.copyOf(dividerPositions);
    }

    public List<DockContainerState> getChildDockContainerStates() {
        return childDockContainerStates;
    }

    public static class DockContainerBranchStateBuilder {

        private final String identifier;
        private final List<DockableState> childDockableStates = new ArrayList<>();
        private @Nullable Boolean pruneWhenEmpty;
        private @Nullable Orientation orientation;
        private final Map<Integer, Double> dividerPositions =
                new LinkedHashMap<>();
        private final List<DockContainerState> childDockContainerStates = new ArrayList<>();

        public DockContainerBranchStateBuilder(final String identifier) {

            this.identifier = requireNonNull(identifier);
        }

        public DockContainerBranchStateBuilder addChildDockableState(final DockableState dockableState) {
            this.childDockableStates.add(requireNonNull(dockableState));
            return this;
        }

        public DockContainerBranchStateBuilder setPruneWhenEmpty(boolean pruneWhenEmpty) {
            this.pruneWhenEmpty = pruneWhenEmpty;
            return this;
        }

        public DockContainerBranchStateBuilder setOrientation(
                final @Nullable Orientation orientation
        ) {
            this.orientation = orientation;
            return this;
        }

        public DockContainerBranchStateBuilder addDividerPosition(
                final Integer dividerIndex,
                final Double dividerPosition
        ) {
            dividerPositions.put(
                    requireNonNull(dividerIndex),
                    requireNonNull(dividerPosition)
            );
            return this;
        }

        public DockContainerBranchStateBuilder addDockContainerState(
                final DockContainerState dockContainerState
        ) {
            this.childDockContainerStates.add(requireNonNull(dockContainerState));
            return this;
        }

        public DockContainerBranchState build() {
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
