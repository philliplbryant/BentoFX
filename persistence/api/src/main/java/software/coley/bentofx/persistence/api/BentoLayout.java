package software.coley.bentofx.persistence.api;

import software.coley.bentofx.Identifiable;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerRootBranch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines the layout of a {code Bento} for persisting it.
 *
 * @author Phil Bryant
 */
public class BentoLayout implements Identifiable {

    private final String identifier;
    private final List<DockContainerRootBranch> rootBranches;
    private final List<DragDropStage> dragDropStages;

    private BentoLayout(
            final String identifier,
            final List<DockContainerRootBranch> rootBranches,
            final List<DragDropStage> dragDropStages
    ) {
        this.identifier = Objects.requireNonNull(identifier);
        this.rootBranches = Objects.requireNonNull(rootBranches);
        this.dragDropStages = Objects.requireNonNull(dragDropStages);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public List<DockContainerRootBranch> getRootBranches() {
        return List.copyOf(rootBranches);
    }

    public List<DragDropStage> getDragDropStages() {
        return List.copyOf(dragDropStages);
    }

    public static class BentoLayoutBuilder {

        private final String bentoId;
        private final List<DockContainerRootBranch> rootBranches =
                new ArrayList<>();
        private final List<DragDropStage> dragDropStages =
                new ArrayList<>();

        public BentoLayoutBuilder(final String bentoId) {
            this.bentoId = Objects.requireNonNull(bentoId);
        }

        public BentoLayoutBuilder addRootBranch(
                final DockContainerRootBranch rootBranch
        ) {
            rootBranches.add(Objects.requireNonNull(rootBranch));
            return this;
        }

        public BentoLayoutBuilder addDragDropStage(
                final DragDropStage dragDropStage
        ) {
            dragDropStages.add(Objects.requireNonNull(dragDropStage));
            return this;
        }

        public BentoLayout build() {
            return new BentoLayout(
                    bentoId,
                    rootBranches,
                    dragDropStages
            );
        }
    }
}
