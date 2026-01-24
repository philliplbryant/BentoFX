/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the layout state of an {@code Identifiable}.
 *
 * @author Phil Bryant
 */
public abstract class IdentifiableState {

    @NotNull private final String identifier;

    protected IdentifiableState(
            final @NotNull String identifier
    ) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    public @NotNull String getIdentifier() {
        return identifier;
    }
}
