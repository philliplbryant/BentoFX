package software.coley.bentofx.persistence.api.provider;

import software.coley.bentofx.Bento;

import java.util.Collection;
import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for resolving
 * {@link Bento} instances.
 *
 * @author Phil Bryant
 */
public interface BentoProvider {

    /**
     * {@return an {@link Optional<Bento>} with the given identifier.}
     * Implementations should return an empty {@link Optional<Bento>} when a
     * {@link Bento} with the specified identifier cannot be found.
     *
     * @param identifier the identifier of the {@link Bento} to be returned.
     */
    Optional<Bento> getBento(String identifier);

    /**
     * {@return a {@link Collection} of all {@link Bento} whose layouts are to be
     * saved and restored.}
     */
    Collection<Bento> getAllBentos();
}
