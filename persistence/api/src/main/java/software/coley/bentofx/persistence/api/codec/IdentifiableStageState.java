package software.coley.bentofx.persistence.api.codec;

import javafx.stage.Modality;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class IdentifiableStageState extends StageState {

    private final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates;

    protected IdentifiableStageState(
            final @NotNull String identifier,
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
            final @NotNull List<DockContainerRootBranchState> rootBranchStates
    ) {
        super(
                identifier,
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
                isFocused);
        this.rootBranchStates = rootBranchStates;

    }

    public @NotNull List<@NotNull DockContainerRootBranchState> getRootBranchStates() {
        return List.copyOf(rootBranchStates);
    }

    public static class StageStateBuilder {

        private final @NotNull String identifier;
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
        private final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates =
                new ArrayList<>();

        public StageStateBuilder(final @NotNull String identifier) {
            this.identifier = requireNonNull(identifier);
        }

        public @NotNull StageStateBuilder setTitle(
                final String title
        ) {
            this.title = title;
            return this;
        }

        public @NotNull StageStateBuilder setX(
                final Double x
        ) {
            this.x = x;
            return this;
        }

        public @NotNull StageStateBuilder setY(
                final Double y
        ) {
            this.y = y;
            return this;
        }

        public @NotNull StageStateBuilder setWidth(
                final Double width
        ) {
            this.width = width;
            return this;
        }

        public @NotNull StageStateBuilder setHeight(
                final Double height
        ) {
            this.height = height;
            return this;
        }

        public @NotNull StageStateBuilder setModality(
                final Modality modality
        ) {
            this.modality = modality;
            return this;
        }

        public @NotNull StageStateBuilder setOpacity(
                final Double opacity
        ) {
            this.opacity = opacity;
            return this;
        }

        public @NotNull StageStateBuilder setIconified(
                final Boolean isIconified
        ) {
            this.isIconified = isIconified;
            return this;
        }

        public @NotNull StageStateBuilder setFullScreen(
                final Boolean isFullScreen
        ) {
            this.isFullScreen = isFullScreen;
            return this;
        }

        public @NotNull StageStateBuilder setMaximized(
                final Boolean isMaximized
        ) {
            this.isMaximized = isMaximized;
            return this;
        }

        public @NotNull StageStateBuilder setAlwaysOnTop(
                final Boolean isAlwaysOnTop
        ) {
            this.isAlwaysOnTop = isAlwaysOnTop;
            return this;
        }

        public @NotNull StageStateBuilder setResizable(
                final Boolean isResizable
        ) {
            this.isResizable = isResizable;
            return this;
        }

        public @NotNull StageStateBuilder setShowing(
                final Boolean isShowing
        ) {
            this.isShowing = isShowing;
            return this;
        }

        public @NotNull StageStateBuilder setFocused(
                final Boolean isFocused
        ) {
            this.isFocused = isFocused;
            return this;
        }

        public @NotNull StageStateBuilder addRootBranchState(
                final @NotNull DockContainerRootBranchState rootBranchState
        ) {
            this.rootBranchStates.add(requireNonNull(rootBranchState));
            return this;
        }

        public @NotNull IdentifiableStageState build() {
            return new IdentifiableStageState(
                    identifier,
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
                    rootBranchStates
                    );
        }
    }
}
