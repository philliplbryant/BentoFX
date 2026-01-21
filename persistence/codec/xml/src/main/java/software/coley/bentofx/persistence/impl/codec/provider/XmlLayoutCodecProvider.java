/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.impl.codec.xml.XmlLayoutCodec;

public class XmlLayoutCodecProvider implements LayoutCodecProvider {

    @Override
    public @NotNull LayoutCodec createLayoutCodec() {
        return new XmlLayoutCodec();
    }
}
