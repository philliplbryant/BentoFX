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

    /**
     * Constructor.
     * @param identifier the {@code DockContainer} identifier.
     * @param pruneWhenEmpty whether the container should be pruned when it holds
     * nothing, {@code null} leaves it unspecified.
     * @param childDockableStates the {@link DockableState}s for the
     * {@code Dockable}s the container holds directly.
     */
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

    /**
     * Builds a {@link DockContainerState}.
     */
    public static class DockContainerStateBuilder {

        /** The {@code DockContainer} identifier. */
        protected final String identifier;

        /** States for the {@code Dockable}s the container holds directly. */
        protected final List<DockableState> childDockableStates = new ArrayList<>();

        /** Whether to prune the container when it holds nothing. */
        protected @Nullable Boolean pruneWhenEmpty;

        /**
         * Constructor.
         * @param identifier the {@code DockContainer} identifier.
         */
        public DockContainerStateBuilder(
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param dockableState the {@link DockableState} to add.
         */
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

        /**
         * {@return the {@link DockContainerState} built from this builder.}
         */
        public DockContainerState build() {
            return new DockContainerState(
                    identifier,
                    pruneWhenEmpty,
                    childDockableStates
            );
        }
    }
}
