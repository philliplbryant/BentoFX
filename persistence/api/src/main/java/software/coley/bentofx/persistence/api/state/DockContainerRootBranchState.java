package software.coley.bentofx.persistence.api.state;

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

    /**
     * Builds a {@link DockContainerRootBranchState}.
     */
    public static class DockContainerRootBranchStateBuilder {

        private final String identifier;
        private final List<DockableState> childDockableStates = new ArrayList<>();
        private @Nullable Boolean pruneWhenEmpty;
        private @Nullable Orientation orientation;
        private final Map<Integer, Double> dividerPositions =
                new LinkedHashMap<>();
        private final List<DockContainerState> childDockContainerStates = new ArrayList<>();

        /**
         * Constructor.
         * @param identifier the {@code DockContainerRootBranch} identifier.
         */
        public DockContainerRootBranchStateBuilder(
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param dockableState the {@link DockableState} to add.
         */
        public DockContainerRootBranchStateBuilder addChildDockableState(final DockableState dockableState) {
            this.childDockableStates.add(requireNonNull(dockableState));
            return this;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param pruneWhenEmpty whether empty branches should be pruned,
         * {@code null} leaves prune-when-empty unspecified.
         */
        public DockContainerRootBranchStateBuilder setPruneWhenEmpty(final @Nullable Boolean pruneWhenEmpty) {
            this.pruneWhenEmpty = pruneWhenEmpty;
            return this;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param orientation the branch orientation, {@code null} leaves
         * {@link Orientation} unspecified.
         */
        public DockContainerRootBranchStateBuilder setOrientation(
                final @Nullable Orientation orientation
        ) {
            this.orientation = orientation;
            return this;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param dividerIndex the index of the divider.
         * @param dividerPosition the divider position, between 0.0 and 1.0 (inclusive).
         */
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

        /**
         * {@return this builder for chaining method calls.}
         * @param dockContainerState the {@link DockContainerState} to add.
         */
        public DockContainerRootBranchStateBuilder addDockContainerState(
                final DockContainerState dockContainerState
        ) {
            this.childDockContainerStates.add(requireNonNull(dockContainerState));
            return this;
        }

        /**
         * {@return the {@link DockContainerRootBranchState} built from this builder.}
         */
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
