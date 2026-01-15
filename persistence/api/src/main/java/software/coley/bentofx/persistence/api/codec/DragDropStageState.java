/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DragDropStageState {

    private final @NotNull Boolean isAutoClosedWhenEmpty;
    private final DockContainerRootBranchState dockContainerRootBranchState;
    private final String title;
    private final Double x;
    private final Double y;
    private final Double width;
    private final Double height;
    private final Boolean isIconified;
    private final Boolean isFullScreen;
    private final Boolean isMaximized;

    private DragDropStageState(
            final @NotNull Boolean isAutoClosedWhenEmpty,
            final DockContainerRootBranchState dockContainerRootBranchState,
            final String title,
            final Double x,
            final Double y,
            final Double width,
            final Double height,
            final Boolean isIconified,
            final Boolean isFullScreen,
            final Boolean isMaximized
    ) {
        this.isAutoClosedWhenEmpty = requireNonNull(isAutoClosedWhenEmpty);
        this.dockContainerRootBranchState = dockContainerRootBranchState;
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isIconified = isIconified;
        this.isFullScreen = isFullScreen;
        this.isMaximized = isMaximized;
    }

    public @NotNull Boolean isAutoClosedWhenEmpty() {
        return isAutoClosedWhenEmpty;
    }

    public @NotNull Optional<DockContainerRootBranchState> getDockContainerRootBranchState() {
        return Optional.of(dockContainerRootBranchState);
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

    public static class DragDropStageStateBuilder {

        private final @NotNull Boolean isAutoClosedWhenEmpty;
        private DockContainerRootBranchState dockContainerRootBranchState;
        private String title;
        private Double x;
        private Double y;
        private Double width;
        private Double height;
        private Boolean isIconified;
        private Boolean isFullScreen;
        private Boolean isMaximized;

        public DragDropStageStateBuilder(
                final @NotNull Boolean isAutoClosedWhenEmpty
        ) {
            this.isAutoClosedWhenEmpty =
                    requireNonNull(isAutoClosedWhenEmpty);
        }

        public @NotNull DragDropStageStateBuilder setDockContainerRootBranchState(
                final DockContainerRootBranchState dockContainerRootBranchState
        ) {
            this.dockContainerRootBranchState = dockContainerRootBranchState;
            return this;
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

        public @NotNull DragDropStageState build() {
            return new DragDropStageState(
                    isAutoClosedWhenEmpty,
                    dockContainerRootBranchState,
                    title,
                    x,
                    y,
                    width,
                    height,
                    isIconified,
                    isFullScreen,
                    isMaximized
            );
        }
    }
}
