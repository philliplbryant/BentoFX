package software.coley.bentofx.persistence.api.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Bento;

import java.util.Collection;
import java.util.Optional;

/**
 * {@code ServiceLoader} compatible Service Provider Interface for getting or
 * creating {@link Bento} instances.
 *
 * @author Phil Bryant
 */
public interface BentoProvider {

    /**
     * Returns the {@link Bento} with the given identifier.
     *
     * @param identifier the identifier of the {@link Bento} to be returned.
     * @return the {@link Bento} with the given identifier.
     */
    @NotNull Optional<@NotNull Bento> getBento(String identifier);

    /**
     * @return a {@link Collection} of all {@link Bento} whose layouts are to be
     * saved and restored.
     */
    @NotNull Collection<@NotNull Bento> getAllBentos();
}
