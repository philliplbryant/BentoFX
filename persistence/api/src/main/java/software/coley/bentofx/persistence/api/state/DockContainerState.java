package software.coley.bentofx.persistence.api.state;

import org.jspecify.annotations.Nullable;

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

    private final List<DockableState> childDockableStates;
    @Nullable
    private final Boolean pruneWhenEmpty;

    protected DockContainerState(
            final String identifier,
            final @Nullable Boolean pruneWhenEmpty,
            final List<DockableState> childDockableStates
    ) {
        super(identifier);
        this.childDockableStates = List.copyOf(childDockableStates);
        this.pruneWhenEmpty = pruneWhenEmpty;
    }

    /**
     * {@return the child dockable states.}
     */
    public List<DockableState> getChildDockableStates() {
        return childDockableStates;
    }

    /**
     * {@return an {@code Optional} specifying whether the dock container should
     * be pruned when empty, an empty {@code Optional} when unspecified.}
     */
    public Optional<Boolean> doPruneWhenEmpty() {
        return Optional.ofNullable(pruneWhenEmpty);
    }

    public static class DockContainerStateBuilder {

        protected final String identifier;
        protected final List<DockableState> childDockableStates = new ArrayList<>();
        protected @Nullable Boolean pruneWhenEmpty;

        public DockContainerStateBuilder(
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public DockContainerStateBuilder addChildDockableState(final DockableState dockableState) {
            this.childDockableStates.add(requireNonNull(dockableState));
            return this;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param pruneWhenEmpty {@code true} to prune when empty, {@code false}
         * otherwise.
         */
        public DockContainerStateBuilder setPruneWhenEmpty(boolean pruneWhenEmpty) {
            this.pruneWhenEmpty = pruneWhenEmpty;
            return this;
        }

        public DockContainerState build() {
            return new DockContainerState(
                    identifier,
                    pruneWhenEmpty,
                    childDockableStates
            );
        }
    }
}
