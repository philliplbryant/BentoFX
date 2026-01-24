/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

/**
 * Indicates conditions that applications might want to catch when the saving
 * and restoring the layout of BentoFX docking components.
 *
 * @author Phil Bryant
 */
public class BentoStateException extends Exception {

    public BentoStateException(final String message) {
        super(message);
    }

    public BentoStateException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
