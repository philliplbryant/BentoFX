/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.json.JsonLayoutCodec;

/**
 * JSON (JavaScript Object Notation) implementation of the
 * {@link LayoutCodecProvider} Service Provider Interface.
 *
 * @author Phil Bryant
 */
public class JsonLayoutCodecProvider implements LayoutCodecProvider {

    @Override
    public @NotNull LayoutCodec createLayoutCodec() {
        return new JsonLayoutCodec();
    }
}
