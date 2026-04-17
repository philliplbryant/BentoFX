package software.coley.bentofx.persistence.impl.codec;

import javafx.geometry.Side;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

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
    private final Boolean isCanSplit;
    @Nullable
    private final Double uncollapsedSizePx;
    @Nullable
    private final Boolean isCollapsed;

    // This is a read only class whose constructor is used to set all attributes.
    @SuppressWarnings("java:S107")
    private DockContainerLeafState(
            final String identifier,
            final @Nullable Boolean doPruneWhenEmpty,
            final List<DockableState> childDockableStates,
            final @Nullable Side side,
            final @Nullable String selectedDockableIdentifier,
            final @Nullable Boolean isResizableWithParent,
            final @Nullable Boolean isCanSplit,
            final @Nullable Double uncollapsedSizePx,
            final @Nullable Boolean isCollapsed
            ) {
        super(
                identifier,
                doPruneWhenEmpty,
                childDockableStates
        );
        this.side = side;
        this.selectedDockableIdentifier = selectedDockableIdentifier;
        this.isResizableWithParent = isResizableWithParent;
        this.isCanSplit = isCanSplit;
        this.uncollapsedSizePx = uncollapsedSizePx;
        this.isCollapsed = isCollapsed;
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

    public Optional<Boolean> isCanSplit() {
        return Optional.ofNullable(isCanSplit);
    }

    public Optional<Double> getUncollapsedSizePx() {
        return Optional.ofNullable(uncollapsedSizePx);
    }

    public Optional<Boolean> isCollapsed() {
        return Optional.ofNullable(isCollapsed);
    }

    public static class DockContainerLeafStateBuilder {

        private final String identifier;
        private final List<DockableState> childDockableStates = new ArrayList<>();
        private @Nullable Boolean pruneWhenEmpty;
        private @Nullable Side side;
        private @Nullable String selectedDockableStateIdentifier;
        private @Nullable Boolean isResizableWithParent;
        private @Nullable Boolean isCanSplit;
        private @Nullable Double uncollapsedSizePx;
        private @Nullable Boolean isCollapsed;

        public DockContainerLeafStateBuilder(
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public DockContainerLeafStateBuilder addChildDockableState(final DockableState dockableState) {
            this.childDockableStates.add(requireNonNull(dockableState));
            return this;
        }

        public DockContainerLeafStateBuilder setPruneWhenEmpty(boolean pruneWhenEmpty) {
            this.pruneWhenEmpty = pruneWhenEmpty;
            return this;
        }

        public DockContainerLeafStateBuilder setSide(final @Nullable Side side) {
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

        public DockContainerLeafStateBuilder setResizableWithParent(
                final Boolean isResizableWithParent
        ) {
            this.isResizableWithParent = isResizableWithParent;
            return this;
        }

        public DockContainerLeafStateBuilder setCanSplit(
                final Boolean isCanSplit
        ) {
            this.isCanSplit = isCanSplit;
            return this;
        }

        public DockContainerLeafStateBuilder setUncollapsedSizePx(
                final Double uncollapsedSizePx
        ) {
            this.uncollapsedSizePx = uncollapsedSizePx;
            return this;
        }

        public DockContainerLeafStateBuilder setCollapsed(
                final Boolean isCollapsed
        ) {
            this.isCollapsed = isCollapsed;
            return this;
        }

        public DockContainerLeafState build() {

            return new DockContainerLeafState(
                    identifier,
                    pruneWhenEmpty,
                    childDockableStates,
                    side,
                    selectedDockableStateIdentifier,
                    isResizableWithParent,
                    isCanSplit,
                    uncollapsedSizePx,
                    isCollapsed
            );
        }
    }
}
