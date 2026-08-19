package software.coley.bentofx.persistence.core.api.state;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Represents the layout state of a {@code DockContainer}.
 *
 * <p>Abstract and sealed to the two shapes a {@code DockContainer} can actually
 * take, mirroring {@code DockContainer} itself, which is
 * {@code sealed ... permits DockContainerBranch, DockContainerLeaf}. Every
 * container is a branch or a leaf, so every container <em>state</em> is a
 * {@link DockContainerBranchState} or a {@link DockContainerLeafState}, and the
 * restorer and the codec mappers both switch over exactly those two. While this
 * class was concrete and publicly buildable, a caller could hand the module a
 * plain {@code DockContainerState} that it would accept, encode, and then
 * silently drop on restore, because neither switch had anything to do with it.
 * Sealing turns that from a runtime warning into a compile error, and lets those
 * switches be verified exhaustive instead of carrying an unreachable
 * {@code default} arm.</p>
 *
 * @author Phil Bryant
 */
public abstract sealed class DockContainerState
        extends IdentifiableState
        permits DockContainerBranchState, DockContainerLeafState {

    private final List<DockableState> childDockableStates;
    @Nullable
    private final Boolean pruneWhenEmpty;

    /**
     * Constructor.
     * @param identifier the {@code DockContainer} identifier.
     * @param pruneWhenEmpty whether the container should be pruned when it holds
     * nothing, {@code null} leaves it unspecified.
     * @param childDockableStates the {@link DockableState}s for the
     * {@code Dockable}s the container holds directly.
     */
    protected DockContainerState(
            final String identifier,
            final @Nullable Boolean pruneWhenEmpty,
            final List<DockableState> childDockableStates
    ) {
        super(identifier);
        this.childDockableStates = List.copyOf(childDockableStates);
        this.pruneWhenEmpty = pruneWhenEmpty;
    }

    /**
     * {@return the child dockable states.}
     */
    public List<DockableState> getChildDockableStates() {
        return childDockableStates;
    }

    /**
     * {@return an {@code Optional} specifying whether the dock container should
     * be pruned when empty, an empty {@code Optional} when unspecified.}
     */
    public Optional<Boolean> doPruneWhenEmpty() {
        return Optional.ofNullable(pruneWhenEmpty);
    }
}
