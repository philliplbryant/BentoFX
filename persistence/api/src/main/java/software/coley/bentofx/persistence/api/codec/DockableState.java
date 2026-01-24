/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Represents the layout state of a {@code Dockable}.
 *
 * @author Phil Bryant
 */
public class DockableState extends IdentifiableState {

    private DockableState(
            final @NotNull String identifier
    ) {
        super(identifier);
    }

    public static class DockableStateBuilder {

        private final @NotNull String identifier;

        public DockableStateBuilder(
                final @NotNull String identifier
        ) {
            this.identifier = requireNonNull(identifier);
        }

        public @NotNull DockableState build() {
            return new DockableState(identifier);
        }
    }
}
