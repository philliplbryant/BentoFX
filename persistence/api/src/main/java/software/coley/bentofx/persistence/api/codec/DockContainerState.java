/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class DockContainerState extends IdentifiableState {

    protected DockContainerState(final @NotNull String identifier) {
        super(identifier);
    }

    public static class DockContainerStateBuilder {

        private final @NotNull String identifier;

        public DockContainerStateBuilder(
                @NotNull
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public @NotNull DockContainerState build() {
            return new DockContainerState(identifier);
        }
    }
}
