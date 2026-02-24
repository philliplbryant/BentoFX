package software.coley.bentofx.persistence.api;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Identifiable;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.codec.IdentifiableStageState;

import java.util.List;
import java.util.Objects;

public class IdentifiableStageLayout implements Identifiable {

    private final @NotNull String identifier;
    private final @NotNull IdentifiableStageState identifiableStageState;
    private final @NotNull List<@NotNull DockContainerRootBranch> rootBranches;

    // TODO BENTO-13: Modify this class to only have a StageLayout
    //  (NOT IdentifiableStageLayout) and a list of RootBranches.
    public IdentifiableStageLayout(
            final @NotNull String identifier,
            final @NotNull IdentifiableStageState identifiableStageState,
            @NotNull List<@NotNull DockContainerRootBranch> rootBranches
    ) {
        this.identifier = Objects.requireNonNull(identifier);
        this.identifiableStageState = Objects.requireNonNull(identifiableStageState);
        this.rootBranches = Objects.requireNonNull(rootBranches);
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    public @NotNull IdentifiableStageState getStageState() {
        return identifiableStageState;
    }

    public @NotNull List<@NotNull DockContainerRootBranch> getRootBranches() {
        return rootBranches;
    }
}
