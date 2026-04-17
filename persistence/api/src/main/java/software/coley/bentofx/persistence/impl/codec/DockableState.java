package software.coley.bentofx.persistence.impl.codec;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableIconFactory;
import software.coley.bentofx.dockable.DockableMenuFactory;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code Dockable}.
 *
 * @author Phil Bryant
 */
public class DockableState extends IdentifiableState {

    private final @Nullable Node dockableNode;
    private final @Nullable String title;
    private final @Nullable DockableIconFactory dockableIconFactory;
    private final @Nullable DockableMenuFactory dockableMenuFactory;
    private final @Nullable Integer dragGroupMask;
    private final @Nullable Boolean isClosable;
    private final @Nullable Consumer<Dockable> dockableConsumer;

    // This is a read-only class whose attributes are set using the constructor.
    @SuppressWarnings("java:S107")
    private DockableState(
            final String identifier,
            final @Nullable Node dockableNode,
            final @Nullable String title,
            final @Nullable DockableIconFactory dockableIconFactory,
            final @Nullable DockableMenuFactory dockableMenuFactory,
            final @Nullable Integer dragGroupMask,
            final @Nullable Boolean isClosable,
            final @Nullable Consumer<Dockable> dockableConsumer
    ) {
        super(identifier);
        this.dockableNode = dockableNode;
        this.title = title;
        this.dockableIconFactory = dockableIconFactory;
        this.dockableMenuFactory = dockableMenuFactory;
        this.dragGroupMask = dragGroupMask;
        this.isClosable = isClosable;
        this.dockableConsumer = dockableConsumer;
    }

    public Optional<Node> getDockableNode() {
        return Optional.ofNullable(dockableNode);
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<DockableIconFactory> getDockableIconFactory() {
        return Optional.ofNullable(dockableIconFactory);
    }

    public Optional<DockableMenuFactory> getDockableMenuFactory() {
        return Optional.ofNullable(dockableMenuFactory);
    }

    public Optional<Integer> getDragGroupMask() {
        return Optional.ofNullable(dragGroupMask);
    }

    public Optional<Boolean> isClosable() {
        return Optional.ofNullable(isClosable);
    }

    public Optional<Consumer<Dockable>> getDockableConsumer() {
        return Optional.ofNullable(dockableConsumer);
    }

    public static class DockableStateBuilder {

        private final String identifier;
        private @Nullable Node dockableNode;
        private @Nullable String title;
        private @Nullable DockableIconFactory dockableIconFactory;
        private @Nullable DockableMenuFactory dockableMenuFactory;
        private @Nullable Integer dragGroupMask;
        private @Nullable Boolean isClosable;
        private @Nullable Consumer<Dockable> dockableConsumer;

        public DockableStateBuilder(
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public DockableStateBuilder setDockableNode(
                final @Nullable Node dockableNode
        ) {
            this.dockableNode = dockableNode;
            return this;
        }

        public DockableStateBuilder setTitle(
                final @Nullable String title
        ) {
            this.title = title;
            return this;
        }

        public DockableStateBuilder setDockableIconFactory(
                final @Nullable DockableIconFactory dockableIconFactory
        ) {
            this.dockableIconFactory = dockableIconFactory;
            return this;
        }

        public DockableStateBuilder setDockableMenuFactory(
                final @Nullable DockableMenuFactory dockableMenuFactory
        ) {
            this.dockableMenuFactory = dockableMenuFactory;
            return this;
        }

        public DockableStateBuilder setDragGroupMask(
                final @Nullable Integer dragGroupMask
        ) {
            this.dragGroupMask = dragGroupMask;
            return this;
        }

        public DockableStateBuilder setClosable(
                @Nullable Boolean isClosable
        ) {
            this.isClosable = isClosable;
            return this;
        }

        public DockableStateBuilder setDockableConsumer(
                @Nullable Consumer<Dockable> dockableConsumer
        ) {
            this.dockableConsumer = dockableConsumer;
            return this;
        }

        public DockableState build() {
            return new DockableState(
                    identifier,
                    dockableNode,
                    title,
                    dockableIconFactory,
                    dockableMenuFactory,
                    dragGroupMask,
                    isClosable,
                    dockableConsumer
            );
        }
    }
}
