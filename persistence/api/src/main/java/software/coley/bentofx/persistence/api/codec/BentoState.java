/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class BentoState extends IdentifiableState {

    private final @NotNull Set<@NotNull DockContainerRootBranchState> rootBranchStates;

    private BentoState(
            final @NotNull String identifier,
            final @NotNull Set<@NotNull DockContainerRootBranchState> rootBranchStates
    ) {
        super(identifier);
        this.rootBranchStates = rootBranchStates;
    }

    public @NotNull Set<@NotNull DockContainerRootBranchState> getRootBranchStates() {
        return Set.copyOf(rootBranchStates);
    }

    public static class BentoStateBuilder {

        private final @NotNull String identifier;
        private final @NotNull Set<@NotNull DockContainerRootBranchState> rootBranchStates =
                new LinkedHashSet<>();

        public BentoStateBuilder(final @NotNull String identifier) {
            this.identifier = identifier;
        }

        public @NotNull BentoStateBuilder addRootBranchState(
                final @NotNull DockContainerRootBranchState... rootBranchState
        ) {
            this.rootBranchStates.addAll(
                    Set.of(requireNonNull(rootBranchState))
            );
            return this;
        }

        public @NotNull BentoState build() {
            return new BentoState(identifier, rootBranchStates);
        }
    }
}
