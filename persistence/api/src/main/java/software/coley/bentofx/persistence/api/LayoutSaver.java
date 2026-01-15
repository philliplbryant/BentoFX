/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api;

import software.coley.bentofx.persistence.api.codec.BentoStateException;

public interface LayoutSaver {

    void saveLayout() throws BentoStateException;
}
