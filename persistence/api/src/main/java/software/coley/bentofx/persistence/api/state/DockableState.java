package software.coley.bentofx.persistence.api.state;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableIconFactory;
import software.coley.bentofx.dockable.DockableMenuFactory;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Represents the properties and factories necessary for constructing a
 * {@code Dockable}.
 *
 * @author Phil Bryant
 */
public class DockableState extends IdentifiableState {

    private final @Nullable Node dockableNode;
    private final @Nullable String title;
    private final @Nullable String tooltip;
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
            final @Nullable String tooltip,
            final @Nullable DockableIconFactory dockableIconFactory,
            final @Nullable DockableMenuFactory dockableMenuFactory,
            final @Nullable Integer dragGroupMask,
            final @Nullable Boolean isClosable,
            final @Nullable Consumer<Dockable> dockableConsumer
    ) {
        super(identifier);
        this.dockableNode = dockableNode;
        this.title = title;
        this.tooltip = tooltip;
        this.dockableIconFactory = dockableIconFactory;
        this.dockableMenuFactory = dockableMenuFactory;
        this.dragGroupMask = dragGroupMask;
        this.isClosable = isClosable;
        this.dockableConsumer = dockableConsumer;
    }

    /**
     * @return the {@link Node} to display when the dockable is selected.
     */
    public Optional<Node> getDockableNode() {
        return Optional.ofNullable(dockableNode);
    }

    /**
     * @return the text for the {@link Dockable}'s {@code Header}.
     */
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    /**
     * @return the text to display when hovering the mouse over the
     * {@link Dockable}'s {@code Header}.
     */
    public Optional<String> getTooltipText() {
        return Optional.ofNullable(tooltip);
    }

    /**
     * @return the {@link DockableIconFactory} for creating the {@link Node}
     * graphic in the {@link Dockable}'s {@code Header}.
     */
    public Optional<DockableIconFactory> getDockableIconFactory() {
        return Optional.ofNullable(dockableIconFactory);
    }

    /**
     * @return {@link DockableMenuFactory} for creating the context menu for a
     * {@link Dockable}.
     */
    public Optional<DockableMenuFactory> getDockableMenuFactory() {
        return Optional.ofNullable(dockableMenuFactory);
    }

    /**
     * @return the drag group mask for the {@link Dockable}.
     */
    public Optional<Integer> getDragGroupMask() {
        return Optional.ofNullable(dragGroupMask);
    }

    /**
     * @return {@code true} if the dockable is closable, {@code false} if not.
     */
    public Optional<Boolean> isClosable() {
        return Optional.ofNullable(isClosable);
    }

    /**
     * @return the {@link Consumer} to call when the {@link Dockable}
     * has been added to the docking layout.
     */
    public Optional<Consumer<Dockable>> getDockableConsumer() {
        return Optional.ofNullable(dockableConsumer);
    }

    public static class DockableStateBuilder {

        private final String identifier;
        private @Nullable Node dockableNode;
        private @Nullable String title;
        private @Nullable String tooltipText;
        private @Nullable DockableIconFactory dockableIconFactory;
        private @Nullable DockableMenuFactory dockableMenuFactory;
        private @Nullable Integer dragGroupMask;
        private @Nullable Boolean isClosable;
        private @Nullable Consumer<Dockable> dockableConsumer;

        /**
         * Constructor.
         * @param identifier the {@link Dockable} identifier.
         */
        public DockableStateBuilder(
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        /**
         * @param dockableNode {@link Node} to display when the dockable is
         * selected.
         * @return this {@link DockableStateBuilder}
         * @see Dockable#setNode(Node)
         */
        public DockableStateBuilder setDockableNode(
                final @Nullable Node dockableNode
        ) {
            this.dockableNode = dockableNode;
            return this;
        }

        /**
         * @param title the text for the {@link Dockable}'s {@code Header}.
         * @return this {@link DockableStateBuilder}
         * @see Dockable#setTitle(String)
         */
        public DockableStateBuilder setTitle(
                final @Nullable String title
        ) {
            this.title = title;
            return this;
        }

        /**
         * @param tooltipText the text to display when hovering the mouse over
         * the {@link Dockable}'s {@code Header}.
         * @return this {@link DockableStateBuilder}
         * @see Tooltip#setText(String)
         * @see Dockable#setTooltip(Tooltip)
         */
        public DockableStateBuilder setTooltipText(
                final @Nullable String tooltipText
        ) {
            this.tooltipText = tooltipText;
            return this;
        }

        /**
         * @param dockableIconFactory {@link DockableIconFactory} for creating a
         * {@link Node} graphic.
         * @return this {@link DockableStateBuilder}
         * @see Dockable#setIconFactory(DockableIconFactory)
         */
        public DockableStateBuilder setDockableIconFactory(
                final @Nullable DockableIconFactory dockableIconFactory
        ) {
            this.dockableIconFactory = dockableIconFactory;
            return this;
        }

        /**
         * @param dockableMenuFactory {@link DockableMenuFactory} for creating
         * the context menu for a {@link Dockable}.
         * @return this {@link DockableStateBuilder}
         * @see Dockable#setContextMenuFactory(DockableMenuFactory)
         */
        public DockableStateBuilder setDockableMenuFactory(
                final @Nullable DockableMenuFactory dockableMenuFactory
        ) {
            this.dockableMenuFactory = dockableMenuFactory;
            return this;
        }

        /**
         * @param dragGroupMask drag group mask.
         * @return this {@link DockableStateBuilder}
         * @see Dockable#setDragGroupMask(int)
         */
        public DockableStateBuilder setDragGroupMask(
                final @Nullable Integer dragGroupMask
        ) {
            this.dragGroupMask = dragGroupMask;
            return this;
        }

        /**
         * @param isClosable {@code true} if the dockable is closable,
         * {@code false} if not closable.
         * @return this {@link DockableStateBuilder}
         * @see Dockable#setClosable(boolean)
         */
        public DockableStateBuilder setClosable(
                @Nullable Boolean isClosable
        ) {
            this.isClosable = isClosable;
            return this;
        }

        /**
         * Specifies the {@link Consumer} to call when the {@link Dockable}
         * represented by the {@link DockableState} has been added to the
         * docking layout.
         * @param dockableConsumer the {@link Consumer} to call when the
         * {@link Dockable} represented by the {@link DockableState} has been
         * added to the docking layout.
         * @return this {@link DockableStateBuilder}
         */
        public DockableStateBuilder setDockableConsumer(
                @Nullable Consumer<Dockable> dockableConsumer
        ) {
            this.dockableConsumer = dockableConsumer;
            return this;
        }

        /**
         * Creates a {@link DockableState}.
         * @return a {@link DockableState}.
         */
        public DockableState build() {
            return new DockableState(
                    identifier,
                    dockableNode,
                    title,
                    tooltipText,
                    dockableIconFactory,
                    dockableMenuFactory,
                    dragGroupMask,
                    isClosable,
                    dockableConsumer
            );
        }
    }
}
