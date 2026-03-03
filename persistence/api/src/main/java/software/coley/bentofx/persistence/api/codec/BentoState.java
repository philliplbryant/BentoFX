/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code Bento}.
 *
 * @author Phil Bryant
 */
public class BentoState extends IdentifiableState {

    private final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates;

    private final @NotNull List<@NotNull DragDropStageState> dragDropStageStates;

    private BentoState(
            final @NotNull String identifier,
            final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates,
            final @NotNull List<@NotNull DragDropStageState> dragDropStageStates
    ) {

        super(identifier);
        this.rootBranchStates = requireNonNull(rootBranchStates);
        this.dragDropStageStates = requireNonNull(dragDropStageStates);
    }

    public @NotNull List<@NotNull DockContainerRootBranchState> getRootBranchStates() {
        return List.copyOf(rootBranchStates);
    }

    public @NotNull List<@NotNull DragDropStageState> getDragDropStageStates() {
        return List.copyOf(dragDropStageStates);
    }

    public static class BentoStateBuilder {

        private final @NotNull String identifier;

        private final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates =
                new ArrayList<>();

        private final @NotNull List<@NotNull DragDropStageState> dragDropStageStates =
                new ArrayList<>();

        public BentoStateBuilder(final @NotNull String identifier) {
            this.identifier = identifier;
        }

        public @NotNull BentoStateBuilder addRootBranchState(
                final @NotNull DockContainerRootBranchState... rootBranchStates
        ) {
            this.rootBranchStates.addAll(
                    List.of(requireNonNull(rootBranchStates))
            );
            return this;
        }

        public @NotNull BentoStateBuilder addDragDropStageState(
                final @NotNull DragDropStageState... dragDropStageStates
        ) {
            this.dragDropStageStates.addAll(
                    List.of(requireNonNull(dragDropStageStates))
            );
            return this;
        }

        public @NotNull BentoState build() {
            return new BentoState(
                    identifier,
                    rootBranchStates,
                    dragDropStageStates
            );
        }
    }
}
