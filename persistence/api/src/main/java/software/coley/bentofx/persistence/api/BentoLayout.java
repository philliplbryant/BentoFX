package software.coley.bentofx.persistence.api;

import software.coley.bentofx.Identifiable;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerRootBranch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines the layout of a {@code Bento} for persisting it.
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
        this.rootBranches = List.copyOf(rootBranches);
        this.dragDropStages = List.copyOf(dragDropStages);
    }

    /**
     * {@return the identifier of the {@code Bento} this layout belongs to.}
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * {@return an immutable {@link List} of the restored
     * {@link DockContainerRootBranch}es, in the order they were added.}
     *
     * <p>These are not attached to a {@code Scene}. The application is
     * responsible for placing them, and must do so before the next save - a
     * {@link DockContainerRootBranch} only registers itself with its
     * {@code Bento} once it has a {@code Scene}, so an unattached branch is
     * invisible to a capture.</p>
     */
    public List<DockContainerRootBranch> getRootBranches() {
        return rootBranches;
    }

    /**
     * {@return an immutable {@link List} of the restored
     * {@link DragDropStage}s, in the order they were added.}
     *
     * <p>These are not shown. Whether a restored stage should be visible is the
     * application's decision.</p>
     */
    public List<DragDropStage> getDragDropStages() {
        return dragDropStages;
    }

    /**
     * Builds a {@link BentoLayout}.
     */
    public static class BentoLayoutBuilder {

        private final String bentoId;
        private final List<DockContainerRootBranch> rootBranches =
                new ArrayList<>();
        private final List<DragDropStage> dragDropStages =
                new ArrayList<>();

        /**
         * Constructor.
         * @param bentoId the identifier of the {@code Bento} this layout
         * belongs to.
         */
        public BentoLayoutBuilder(final String bentoId) {
            this.bentoId = Objects.requireNonNull(bentoId);
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param rootBranch the {@link DockContainerRootBranch} to add.
         */
        public BentoLayoutBuilder addRootBranch(
                final DockContainerRootBranch rootBranch
        ) {
            rootBranches.add(Objects.requireNonNull(rootBranch));
            return this;
        }

        /**
         * {@return this builder for chaining method calls.}
         * @param dragDropStage the {@link DragDropStage} to add.
         */
        public BentoLayoutBuilder addDragDropStage(
                final DragDropStage dragDropStage
        ) {
            dragDropStages.add(Objects.requireNonNull(dragDropStage));
            return this;
        }

        /**
         * {@return the {@link BentoLayout} built from this builder.}
         */
        public BentoLayout build() {
            return new BentoLayout(
                    bentoId,
                    rootBranches,
                    dragDropStages
            );
        }
    }
}
