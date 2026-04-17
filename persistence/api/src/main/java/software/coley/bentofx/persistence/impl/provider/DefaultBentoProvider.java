package software.coley.bentofx.persistence.impl.provider;

import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.provider.BentoProvider;

import java.util.*;

/**
 * Default implementation of {@link BentoProvider} that merely collects and
 * retrieves {@code Bento} instances using a weak reference their identifiers.
 *
 * @author Phil Bryant
 */
public class DefaultBentoProvider implements BentoProvider {

    private final Map<String, Bento> bentoMap =
            new WeakHashMap<>();

    public DefaultBentoProvider() {
    }

    public DefaultBentoProvider(final Bento... bentos) {
        for(final Bento bento : bentos) {
            addBento(bento);
        }
    }

    public void addBento(final Bento bento) {
        bentoMap.put(bento.getIdentifier(), bento);
    }

    @Override
    public Optional<Bento> getBento(String identifier) {
        return Optional.ofNullable(bentoMap.get(identifier));
    }

    @Override
    public Collection<Bento> getAllBentos() {
        return new ArrayList<>(bentoMap.values());
    }
}
