package software.coley.bentofx.persistence.api;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Identifiable;
import software.coley.bentofx.control.DragDropStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BentoLayout implements Identifiable {

    private final @NotNull String identifier;
    private final @NotNull List<@NotNull IdentifiableStageLayout> stageLayouts;
    private final @NotNull List<@NotNull DragDropStage> dragDropStages;

    private BentoLayout(
            final @NotNull String identifier,
            final @NotNull List<@NotNull IdentifiableStageLayout> stageLayouts,
            final @NotNull List<@NotNull DragDropStage> dragDropStages
    ) {
        this.identifier = Objects.requireNonNull(identifier);
        this.stageLayouts = Objects.requireNonNull(stageLayouts);
        this.dragDropStages = Objects.requireNonNull(dragDropStages);
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    public @NotNull List<@NotNull IdentifiableStageLayout> getStageLayouts() {
        return List.copyOf(stageLayouts);
    }

    public @NotNull List<@NotNull DragDropStage> getDragDropStages() {
        return List.copyOf(dragDropStages);
    }

    public static class BentoLayoutBuilder {

        private final @NotNull String bentoId;
        private final @NotNull List<@NotNull IdentifiableStageLayout> stageLayouts =
                new ArrayList<>();
        private final @NotNull List<@NotNull DragDropStage> dragDropStages =
                new ArrayList<>();

        public BentoLayoutBuilder(final @NotNull String bentoId) {
            this.bentoId = Objects.requireNonNull(bentoId);
        }

        public BentoLayoutBuilder addStageLayout(
                final @NotNull IdentifiableStageLayout stageLayout
        ) {
            stageLayouts.add(Objects.requireNonNull(stageLayout));
            return this;
        }

        public BentoLayoutBuilder addDragDropStage(
                final @NotNull DragDropStage dragDropStage
        ) {
            dragDropStages.add(Objects.requireNonNull(dragDropStage));
            return this;
        }

        public BentoLayout build() {
            return new BentoLayout(
                    bentoId,
                    stageLayouts,
                    dragDropStages
            );
        }
    }
}
