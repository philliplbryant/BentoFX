/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class DockableState extends IdentifiableState {

    private DockableState(
            @NotNull
            final String identifier
    ) {
        super(identifier);
    }

    public static class DockableStateBuilder {

        private final @NotNull String identifier;

        public DockableStateBuilder(
                @NotNull
                final String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public @NotNull DockableState build() {
            return new DockableState(identifier);
        }
    }
}
