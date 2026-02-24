/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.api.codec;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Codec for encoding/decoding {@link BentoState} to/from some persistence format (XML/JSON/etc).
 *
 * @author Phil Bryant
 */
public interface LayoutCodec {

    /**
     * Returns an identifier used to differentiate this {@link LayoutCodec}
     * implementation from other {@link LayoutCodec} implementations. Could be
     * usable as a file extension.
     * @return an identifier used to differentiate this {@link LayoutCodec}.
     */
    String getIdentifier();

    /**
     * Encode the {@link BentoState} and write it to the {@link OutputStream}.
     *
     * @param bentoStates the {@link BentoState}s to be encoded.
     * @param outputStream the {@link OutputStream} where the encoded
     * {@link BentoState} is to be written.
     * @throws BentoStateException on error
     */
    void encode(
            final @NotNull List<@NotNull BentoState> bentoStates,
            final @NotNull OutputStream outputStream
    ) throws BentoStateException;

    /**
     * Read InputStream
     *
     * @param inputStream Stream to which {@link BentoState} was written
     * @return {@link BentoState}
     * @throws BentoStateException on error
     */
    @NotNull List<@NotNull BentoState> decode(
            @NotNull final InputStream inputStream
    ) throws BentoStateException;
}
