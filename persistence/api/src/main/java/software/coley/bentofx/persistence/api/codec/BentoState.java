/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code Bento}.
 *
 * @author Phil Bryant
 */
public class BentoState {

    private final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates;

    private BentoState(
            final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates
    ) {
        this.rootBranchStates =
                List.of(rootBranchStates.toArray(
                        new DockContainerRootBranchState[0])
                );
    }

    public @NotNull Set<@NotNull DockContainerRootBranchState> getRootBranchStates() {
        return Set.copyOf(rootBranchStates);
    }

    public static class BentoStateBuilder {

        private final @NotNull List<@NotNull DockContainerRootBranchState> rootBranchStates =
                new ArrayList<>();

        public @NotNull BentoStateBuilder addRootBranchState(
                final @NotNull DockContainerRootBranchState... rootBranchState
        ) {
            this.rootBranchStates.addAll(
                    List.of(requireNonNull(rootBranchState))
            );
            return this;
        }

        public @NotNull BentoState build() {
            return new BentoState(rootBranchStates);
        }
    }
}
