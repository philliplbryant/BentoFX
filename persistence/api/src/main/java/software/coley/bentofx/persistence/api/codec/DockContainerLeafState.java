/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import javafx.geometry.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Represents the layout state of a {@code DockContainerLeaf}.
 *
 * @author Phil Bryant
 */
public class DockContainerLeafState extends DockContainerState {

    @Nullable
    private final Side side;
    @Nullable
    private final String selectedDockableIdentifier;
    @Nullable
    private final Boolean isResizableWithParent;
    @Nullable
    private final Boolean canSplit;

    private DockContainerLeafState(
            final @NotNull String identifier,
            final @Nullable DragDropStageState parent,
            final @Nullable Boolean doPruneWhenEmpty,
            final @NotNull List<DockableState> childDockableStates,
            final @Nullable Side side,
            final @Nullable String selectedDockableIdentifier,
            final @Nullable Boolean isResizableWithParent,
            final @Nullable Boolean canSplit
    ) {
        super(
                identifier,
                parent,
                doPruneWhenEmpty,
                childDockableStates
        );
        this.side = side;
        this.selectedDockableIdentifier = selectedDockableIdentifier;
        this.isResizableWithParent = isResizableWithParent;
        this.canSplit = canSplit;
    }

    public Optional<Side> getSide() {
        return Optional.ofNullable(side);
    }

    public Optional<String> getSelectedDockableIdentifier() {
        return Optional.ofNullable(selectedDockableIdentifier);
    }

    public Optional<Boolean> isResizableWithParent() {
        return Optional.ofNullable(isResizableWithParent);
    }

    public Optional<Boolean> canSplit() {
        return Optional.ofNullable(canSplit);
    }

    public static class DockContainerLeafStateBuilder extends DockContainerStateBuilder {

        @Nullable
        private Side side;
        @Nullable
        private String selectedDockableStateIdentifier;
        @Nullable
        private Boolean isResizableWithParent;
        @Nullable
        private Boolean isCanSplit;

        public DockContainerLeafStateBuilder(
                final @NotNull String identifier
        ) {
            super(identifier);
        }

        public @NotNull DockContainerLeafStateBuilder setSide(final Side side) {
            this.side = side;
            return this;
        }

        public @NotNull DockContainerLeafStateBuilder setSelectedDockableStateIdentifier(
                final String selectedDockableStateIdentifier
        ) {
            this.selectedDockableStateIdentifier =
                    selectedDockableStateIdentifier;
            return this;
        }

        public @NotNull DockContainerLeafStateBuilder setResizableWithParent(
                final Boolean isResizableWithParent
        ) {
            this.isResizableWithParent = isResizableWithParent;
            return this;
        }

        public @NotNull DockContainerLeafStateBuilder setCanSplit(
                final Boolean isCanSplit
        ) {
            this.isCanSplit = isCanSplit;
            return this;
        }

        public @NotNull DockContainerLeafState build() {

            return new DockContainerLeafState(
                    identifier,
                    parent,
                    pruneWhenEmpty,
                    childDockableStates,
                    side,
                    selectedDockableStateIdentifier,
                    isResizableWithParent,
                    isCanSplit
            );
        }
    }
}
