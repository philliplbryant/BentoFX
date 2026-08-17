package software.coley.bentofx.persistence.testfixtures.codec;

import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.state.BentoState;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Simple {@link LayoutCodec} implementation for provider-selection tests.
 *
 * <p>It encodes nothing and decodes to nothing, which is enough for a test that
 * only cares which codec was chosen</p>
 *
 * @param identifier the identifier this codec answers to.
 *
 * @author Phil Bryant
 * @see InMemoryLayoutCodec} for encoding and decoding what was encoded
 */
public record TestLayoutCodec(String identifier) implements LayoutCodec {

	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Discards the supplied states, writing nothing.
	 *
	 * @param bentoStates ignored.
	 * @param outputStream ignored, and left unwritten.
	 */
	@Override
	public void encode(
			final List<BentoState> bentoStates,
			final OutputStream outputStream
	) {
		// no-op
	}

	/**
	 * {@return an empty list, whatever the stream holds.}
	 *
	 * @param inputStream ignored, and left unread.
	 */
	@Override
	public List<BentoState> decode(
			final InputStream inputStream
	) {
		return List.of();
	}
}
