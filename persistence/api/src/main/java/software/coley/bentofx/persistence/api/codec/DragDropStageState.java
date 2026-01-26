/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code DragDropStage}.
 *
 * @author Phil Bryant
 */
public class DragDropStageState {

    private final @NotNull Boolean isAutoClosedWhenEmpty;
    private final @Nullable String title;
    private final @Nullable Double x;
    private final @Nullable Double y;
    private final @Nullable Double width;
    private final @Nullable Double height;
    private final @Nullable Boolean isIconified;
    private final @Nullable Boolean isFullScreen;
    private final @Nullable Boolean isMaximized;
    private final @NotNull DockContainerRootBranchState dockContainerRootBranchState;

    // Ignore the number or constructor parameters; this is a read-only class
    // whose member attributes must be set using the constructor.
    @SuppressWarnings("java:S107")
    private DragDropStageState(
            final @NotNull Boolean isAutoClosedWhenEmpty,
            final @Nullable String title,
            final @Nullable Double x,
            final @Nullable Double y,
            final @Nullable Double width,
            final @Nullable Double height,
            final @Nullable Boolean isIconified,
            final @Nullable Boolean isFullScreen,
            final @Nullable Boolean isMaximized,
            final @NotNull DockContainerRootBranchState dockContainerRootBranchState
    ) {
        this.isAutoClosedWhenEmpty = requireNonNull(isAutoClosedWhenEmpty);
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isIconified = isIconified;
        this.isFullScreen = isFullScreen;
        this.isMaximized = isMaximized;
        this.dockContainerRootBranchState = dockContainerRootBranchState;
    }

    public @NotNull Boolean isAutoClosedWhenEmpty() {
        return isAutoClosedWhenEmpty;
    }

    public @NotNull Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public @NotNull Optional<Double> getX() {
        return Optional.ofNullable(x);
    }

    public @NotNull Optional<Double> getY() {
        return Optional.ofNullable(y);
    }

    public @NotNull Optional<Double> getWidth() {
        return Optional.ofNullable(width);
    }

    public @NotNull Optional<Double> getHeight() {
        return Optional.ofNullable(height);
    }

    public @NotNull Optional<Boolean> isIconified() {
        return Optional.ofNullable(isIconified);
    }

    public @NotNull Optional<Boolean> isFullScreen() {
        return Optional.ofNullable(isFullScreen);
    }

    public @NotNull Optional<Boolean> isMaximized() {
        return Optional.ofNullable(isMaximized);
    }

    public @NotNull Optional<DockContainerRootBranchState> getDockContainerRootBranchState() {
        return Optional.of(dockContainerRootBranchState);
    }

    public static class DragDropStageStateBuilder {

        private final @NotNull Boolean isAutoClosedWhenEmpty;
        private @Nullable DockContainerRootBranchState dockContainerRootBranchState;
        private @Nullable String title;
        private @Nullable Double x;
        private @Nullable Double y;
        private @Nullable Double width;
        private @Nullable Double height;
        private @Nullable Boolean isIconified;
        private @Nullable Boolean isFullScreen;
        private @Nullable Boolean isMaximized;

        public DragDropStageStateBuilder(
                final @NotNull Boolean isAutoClosedWhenEmpty
        ) {
            this.isAutoClosedWhenEmpty =
                    requireNonNull(isAutoClosedWhenEmpty);
        }

        public @NotNull DragDropStageStateBuilder setTitle(
                final String title
        ) {
            this.title = title;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setX(
                final Double x
        ) {
            this.x = x;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setY(
                final Double y
        ) {
            this.y = y;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setWidth(
                final Double width
        ) {
            this.width = width;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setHeight(
                final Double height
        ) {
            this.height = height;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setIsIconified(
                final Boolean isIconified
        ) {
            this.isIconified = isIconified;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setIsFullScreen(
                final Boolean isFullScreen
        ) {
            this.isFullScreen = isFullScreen;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setIsMaximized(
                final Boolean isMaximized
        ) {
            this.isMaximized = isMaximized;
            return this;
        }

        public @NotNull DragDropStageStateBuilder setDockContainerRootBranchState(
                final @Nullable DockContainerRootBranchState dockContainerRootBranchState
        ) {
            this.dockContainerRootBranchState = dockContainerRootBranchState;
            return this;
        }

        public @NotNull DragDropStageState build() {
            return new DragDropStageState(
                    isAutoClosedWhenEmpty,
                    title,
                    x,
                    y,
                    width,
                    height,
                    isIconified,
                    isFullScreen,
                    isMaximized,
                    dockContainerRootBranchState
            );
        }
    }
}
