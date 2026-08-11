package software.coley.bentofx.persistence.api.state;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code Bento}.
 *
 * @author Phil Bryant
 */
public class BentoState extends IdentifiableState {

    private final List<DockContainerRootBranchState> rootBranchStates;

    private final List<DragDropStageState> dragDropStageStates;

    private BentoState(
            final String identifier,
            final List<DockContainerRootBranchState> rootBranchStates,
            final List<DragDropStageState> dragDropStageStates
    ) {

        super(identifier);
        this.rootBranchStates = List.copyOf(rootBranchStates);
        this.dragDropStageStates = List.copyOf(dragDropStageStates);
    }

    /**
     * {@return an immutable {@link List} of the
     * {@link DockContainerRootBranchState}s for the {@code Bento}'s root
     * branches. Empty when none were specified.}
     */
    public List<DockContainerRootBranchState> getRootBranchStates() {
        return rootBranchStates;
    }

    /**
     * {@return an immutable {@link List} of the {@link DragDropStageState}s for
     * the {@code Bento}'s drag/drop stages. Empty when none were specified.}
     */
    public List<DragDropStageState> getDragDropStageStates() {
        return dragDropStageStates;
    }

    /**
     * Builds a {@link BentoState}.
     */
    public static class BentoStateBuilder {

        private final String identifier;

        private final List<DockContainerRootBranchState> rootBranchStates =
                new ArrayList<>();

        private final List<DragDropStageState> dragDropStageStates =
                new ArrayList<>();

        /**
         * Constructor.
         * @param identifier the {@code Bento} identifier.
         */
        public BentoStateBuilder(final String identifier) {
            this.identifier = identifier;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param rootBranchState the {@link DockContainerRootBranchState} to add.
         */
        public BentoStateBuilder addRootBranchState(
                final DockContainerRootBranchState rootBranchState
        ) {
            this.rootBranchStates.add(requireNonNull(rootBranchState));
            return this;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param dragDropStageState the {@link DragDropStageState} to add.
         */
        public BentoStateBuilder addDragDropStageState(
                final DragDropStageState dragDropStageState
        ) {
            this.dragDropStageStates.add(requireNonNull(dragDropStageState));
            return this;
        }

        /**
         * {@return the {@link BentoState} built from this builder.}
         */
        public BentoState build() {
            return new BentoState(
                    identifier,
                    rootBranchStates,
                    dragDropStageStates
            );
        }
    }
}
