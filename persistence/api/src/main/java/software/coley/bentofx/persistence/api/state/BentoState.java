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

    public List<DockContainerRootBranchState> getRootBranchStates() {
        return rootBranchStates;
    }

    public List<DragDropStageState> getDragDropStageStates() {
        return dragDropStageStates;
    }

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
         * @param rootBranchStates the {@link DockContainerRootBranchState} to add.
         */
        public BentoStateBuilder addRootBranchState(
                final DockContainerRootBranchState... rootBranchStates
        ) {
            this.rootBranchStates.addAll(
                    List.of(requireNonNull(rootBranchStates))
            );
            return this;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param dragDropStageStates the {@link DragDropStageState} to add.         *
         */
        public BentoStateBuilder addDragDropStageState(
                final DragDropStageState... dragDropStageStates
        ) {
            this.dragDropStageStates.addAll(
                    List.of(requireNonNull(dragDropStageStates))
            );
            return this;
        }

        public BentoState build() {
            return new BentoState(
                    identifier,
                    rootBranchStates,
                    dragDropStageStates
            );
        }
    }
}
