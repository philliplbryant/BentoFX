package software.coley.bentofx.persistence.api.state;

import javafx.stage.Modality;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code DragDropStage}.
 *
 * @author Phil Bryant
 */
public class DragDropStageState {

    private final @Nullable String title;
    private final @Nullable Double x;
    private final @Nullable Double y;
    private final @Nullable Double width;
    private final @Nullable Double height;
    private final @Nullable Modality modality;
    private final @Nullable Double opacity;
    private final @Nullable Boolean isIconified;
    private final @Nullable Boolean isFullScreen;
    private final @Nullable Boolean isMaximized;
    private final @Nullable Boolean isAlwaysOnTop;
    private final @Nullable Boolean isResizable;
    private final @Nullable Boolean isShowing;
    private final @Nullable Boolean isFocused;
    private final Boolean isAutoClosedWhenEmpty;
    private final @Nullable DockContainerRootBranchState dockContainerRootBranchState;

    // Ignore the number or constructor parameters; this is a read-only class
    // whose member attributes must be set using the constructor.
    @SuppressWarnings("java:S107")
    private DragDropStageState(
            final @Nullable String title,
            final @Nullable Double x,
            final @Nullable Double y,
            final @Nullable Double width,
            final @Nullable Double height,
            final @Nullable Modality modality,
            final @Nullable Double opacity,
            final @Nullable Boolean isIconified,
            final @Nullable Boolean isFullScreen,
            final @Nullable Boolean isMaximized,
            final @Nullable Boolean isAlwaysOnTop,
            final @Nullable Boolean isResizable,
            final @Nullable Boolean isShowing,
            final @Nullable Boolean isFocused,
            final Boolean isAutoClosedWhenEmpty,
            final @Nullable DockContainerRootBranchState dockContainerRootBranchState
    ) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.modality = modality;
        this.opacity = opacity;
        this.isIconified = isIconified;
        this.isFullScreen = isFullScreen;
        this.isMaximized = isMaximized;
        this.isAlwaysOnTop = isAlwaysOnTop;
        this.isResizable = isResizable;
        this.isShowing = isShowing;
        this.isFocused = isFocused;
        this.isAutoClosedWhenEmpty = requireNonNull(isAutoClosedWhenEmpty);
        this.dockContainerRootBranchState = dockContainerRootBranchState;
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<Double> getX() {
        return Optional.ofNullable(x);
    }

    public Optional<Double> getY() {
        return Optional.ofNullable(y);
    }

    public Optional<Double> getWidth() {
        return Optional.ofNullable(width);
    }

    public Optional<Double> getHeight() {
        return Optional.ofNullable(height);
    }

    public Optional<Modality> getModality() {
        return Optional.ofNullable(modality);
    }

    public Optional<Double> getOpacity() {
        return Optional.ofNullable(opacity);
    }

    public Optional<Boolean> isIconified() {
        return Optional.ofNullable(isIconified);
    }

    public Optional<Boolean> isFullScreen() {
        return Optional.ofNullable(isFullScreen);
    }

    public Optional<Boolean> isMaximized() {
        return Optional.ofNullable(isMaximized);
    }

    public Optional<Boolean> isAlwaysOnTop() {
        return Optional.ofNullable(isAlwaysOnTop);
    }

    public Optional<Boolean> isResizable() {
        return Optional.ofNullable(isResizable);
    }

    public Optional<Boolean> isShowing() {
        return Optional.ofNullable(isShowing);
    }

    public Optional<Boolean> isFocused() {
        return Optional.ofNullable(isFocused);
    }

    public Boolean isAutoClosedWhenEmpty() {
        return isAutoClosedWhenEmpty;
    }

    public Optional<DockContainerRootBranchState> getDockContainerRootBranchState() {
        return Optional.ofNullable(dockContainerRootBranchState);
    }

    public static class DragDropStageStateBuilder {

        private final Boolean isAutoClosedWhenEmpty;
        private @Nullable DockContainerRootBranchState dockContainerRootBranchState;
        private @Nullable String title;
        private @Nullable Double x;
        private @Nullable Double y;
        private @Nullable Double width;
        private @Nullable Double height;
        private @Nullable Modality modality;
        private @Nullable Double opacity;
        private @Nullable Boolean isIconified;
        private @Nullable Boolean isFullScreen;
        private @Nullable Boolean isMaximized;
        private @Nullable Boolean isAlwaysOnTop;
        private @Nullable Boolean isResizable;
        private @Nullable Boolean isShowing;
        private @Nullable Boolean isFocused;

        public DragDropStageStateBuilder(
                final Boolean isAutoClosedWhenEmpty
        ) {
            this.isAutoClosedWhenEmpty =
                    requireNonNull(isAutoClosedWhenEmpty);
        }

        public DragDropStageStateBuilder setDockContainerRootBranchState(
                final @Nullable DockContainerRootBranchState dockContainerRootBranchState
        ) {
            this.dockContainerRootBranchState = dockContainerRootBranchState;
            return this;
        }

        public DragDropStageStateBuilder setTitle(
                final String title
        ) {
            this.title = title;
            return this;
        }

        public DragDropStageStateBuilder setX(
                final Double x
        ) {
            this.x = x;
            return this;
        }

        public DragDropStageStateBuilder setY(
                final Double y
        ) {
            this.y = y;
            return this;
        }

        public DragDropStageStateBuilder setWidth(
                final Double width
        ) {
            this.width = width;
            return this;
        }

        public DragDropStageStateBuilder setHeight(
                final Double height
        ) {
            this.height = height;
            return this;
        }

        public DragDropStageStateBuilder setModality(
                final Modality modality
        ) {
            this.modality = modality;
            return this;
        }

        public DragDropStageStateBuilder setOpacity(
                final Double opacity
        ) {
            this.opacity = opacity;
            return this;
        }

        public DragDropStageStateBuilder setIconified(
                final Boolean isIconified
        ) {
            this.isIconified = isIconified;
            return this;
        }

        public DragDropStageStateBuilder setFullScreen(
                final Boolean isFullScreen
        ) {
            this.isFullScreen = isFullScreen;
            return this;
        }

        public DragDropStageStateBuilder setMaximized(
                final Boolean isMaximized
        ) {
            this.isMaximized = isMaximized;
            return this;
        }

        public DragDropStageStateBuilder setAlwaysOnTop(
                final Boolean isAlwaysOnTop
        ) {
            this.isAlwaysOnTop = isAlwaysOnTop;
            return this;
        }

        public DragDropStageStateBuilder setResizable(
                final Boolean isResizable
        ) {
            this.isResizable = isResizable;
            return this;
        }

        public DragDropStageStateBuilder setShowing(
                final Boolean isShowing
        ) {
            this.isShowing = isShowing;
            return this;
        }

        public DragDropStageStateBuilder setFocused(
                final Boolean isFocused
        ) {
            this.isFocused = isFocused;
            return this;
        }

        public DragDropStageState build() {
            return new DragDropStageState(
                    title,
                    x,
                    y,
                    width,
                    height,
                    modality,
                    opacity,
                    isIconified,
                    isFullScreen,
                    isMaximized,
                    isAlwaysOnTop,
                    isResizable,
                    isShowing,
                    isFocused,
                    isAutoClosedWhenEmpty,
                    dockContainerRootBranchState
            );
        }
    }
}
