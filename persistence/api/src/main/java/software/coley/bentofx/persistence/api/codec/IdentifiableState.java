/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class IdentifiableState {

    private final @NotNull String identifier;

    protected IdentifiableState(
            @NotNull
            final String identifier
    ) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    public @NotNull String getIdentifier() {
        return identifier;
    }
}
