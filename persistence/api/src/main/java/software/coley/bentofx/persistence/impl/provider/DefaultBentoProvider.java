package software.coley.bentofx.persistence.impl.provider;

import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.provider.BentoProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link BentoProvider} that merely collects and
 * retrieves {@code Bento} instances using a weak reference their identifiers.
 *
 * @author Phil Bryant
 */
public class DefaultBentoProvider implements BentoProvider {

    private final Map<String, WeakReference<Bento>> bentoMap =
            new ConcurrentHashMap<>();

    public DefaultBentoProvider() {
    }

    public DefaultBentoProvider(final Bento... bentos) {
        for(final Bento bento : bentos) {
            addBento(bento);
        }
    }

    public void addBento(final Bento bento) {
        bentoMap.put(
                bento.getIdentifier(),
                new WeakReference<>(bento)
        );
    }

    @Override
    public Optional<Bento> getBento(String identifier) {
        final WeakReference<Bento> bentoReference = bentoMap.get(identifier);
        return Optional.ofNullable(
                bentoReference == null ? null : bentoReference.get()
        );
    }

    @Override
    public Collection<Bento> getAllBentos() {
        return bentoMap.values().stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
