package software.coley.bentofx.persistence.api;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Identifiable;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.codec.IdentifiableStageState;

import java.util.List;
import java.util.Objects;

/**
 * Similar to {@link IdentifiableStageState} but contains a list of
 * {@link DockContainerRootBranch} restored from the
 * {@link IdentifiableStageState#getRootBranchStates()}.
 */
public class IdentifiableStageLayout implements Identifiable {

    private final @NotNull IdentifiableStageState identifiableStageState;
    private final @NotNull List<@NotNull DockContainerRootBranch> rootBranches;

    public IdentifiableStageLayout(
            final @NotNull IdentifiableStageState identifiableStageState,
            @NotNull List<@NotNull DockContainerRootBranch> rootBranches
    ) {
        this.identifiableStageState = Objects.requireNonNull(identifiableStageState);
        this.rootBranches = Objects.requireNonNull(rootBranches);
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifiableStageState.getIdentifier();
    }

    public @NotNull IdentifiableStageState getStageState() {
        return identifiableStageState;
    }

    public @NotNull List<@NotNull DockContainerRootBranch> getRootBranches() {
        return rootBranches;
    }
}
