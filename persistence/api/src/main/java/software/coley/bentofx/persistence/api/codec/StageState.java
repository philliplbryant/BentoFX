package software.coley.bentofx.persistence.api.codec;

import javafx.stage.Modality;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StageState extends IdentifiableState {

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

    // This is a read-only class whose attributes are set using the constructor.
    @SuppressWarnings("java:S107")
    protected StageState(
            @NotNull String identifier,
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
            final @Nullable Boolean isFocused
            ) {

        super(identifier);
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

    public @NotNull Optional<Modality> getModality() {
        return Optional.ofNullable(modality);
    }

    public @NotNull Optional<Double> getOpacity() {
        return Optional.ofNullable(opacity);
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

    public @NotNull Optional<Boolean> isAlwaysOnTop() {
        return Optional.ofNullable(isAlwaysOnTop);
    }

    public @NotNull Optional<Boolean> isResizable() {
        return Optional.ofNullable(isResizable);
    }

    public @NotNull Optional<Boolean> isShowing() {
        return Optional.ofNullable(isShowing);
    }

    public @NotNull Optional<Boolean> isFocused() {
        return Optional.ofNullable(isFocused);
    }
}
