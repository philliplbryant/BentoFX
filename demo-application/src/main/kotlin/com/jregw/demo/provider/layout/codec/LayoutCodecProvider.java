/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package com.jregw.demo.provider.layout.codec;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;

public interface LayoutCodecProvider {

    LayoutCodec createLayoutCodec();
}
