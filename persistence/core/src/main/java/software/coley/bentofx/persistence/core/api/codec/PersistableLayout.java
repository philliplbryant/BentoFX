package software.coley.bentofx.persistence.core.api.codec;

import org.jspecify.annotations.Nullable;
import software.coley.bentofx.persistence.core.api.state.BentoState;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * What a {@link LayoutCodec} encodes and decodes: the state that makes up a
 * layout, together with the layout-level metadata that travels with it.
 *
 * <p>The state is the {@link BentoState} list a save captured. The one piece of
 * metadata so far is an optional display name, the human-readable label a user
 * gave a layout they saved. It rides inside the encoded layout so that a stored
 * layout carries its own name - a file copied to another machine, or a row read
 * back from a database, arrives knowing what it is called - rather than an
 * application having to keep names in a table of its own.</p>
 *
 * <p>A layout saved without a name, such as the session layout, has no display
 * name. The state list is never {@code null}, though it may be empty.</p>
 *
 * @param displayName the layout's human-readable name, or {@code null} when it
 * has none.
 * @param bentoStates the state that makes up the layout. Copied on
 * construction, so this record does not share a list with its caller.
 *
 * @author Phil Bryant
 */
public record PersistableLayout(
        @Nullable String displayName,
        List<BentoState> bentoStates
) {

    /**
     * Canonical constructor, copying the state list so the layout is immutable.
     */
    public PersistableLayout {
        requireNonNull(bentoStates, "bentoStates");
        bentoStates = List.copyOf(bentoStates);
    }

    /**
     * {@return a layout of the supplied state with no display name}, for one
     * that is not saved under a name of its own.
     *
     * @param bentoStates the state that makes up the layout.
     */
    public static PersistableLayout of(final List<BentoState> bentoStates) {
        return new PersistableLayout(null, bentoStates);
    }

    /**
     * {@return the layout's display name, if it has one.}
     */
    public Optional<String> findDisplayName() {
        return Optional.ofNullable(displayName);
    }
}
