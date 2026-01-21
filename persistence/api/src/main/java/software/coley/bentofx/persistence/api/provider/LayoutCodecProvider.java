/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for creating
 * {@link LayoutCodec} implementations.
 */
public interface LayoutCodecProvider {

    /**
     * Creates a {@link LayoutCodec}.
     * @return a {@link LayoutCodec}
     */
    @NotNull LayoutCodec createLayoutCodec();
}
