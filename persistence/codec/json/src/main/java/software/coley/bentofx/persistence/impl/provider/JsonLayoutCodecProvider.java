/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.provider;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.codec.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.json.JsonLayoutCodec;

public class JsonLayoutCodecProvider implements LayoutCodecProvider {

    @Override
    public LayoutCodec createLayoutCodec() {
        return new JsonLayoutCodec();
    }
}
