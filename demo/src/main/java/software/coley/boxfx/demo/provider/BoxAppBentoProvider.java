package software.coley.boxfx.demo.provider;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.provider.BentoProvider;

import java.util.*;

public class BoxAppBentoProvider implements BentoProvider {

    private final Map<@NotNull String, @NotNull Bento> BENTO_MAP =
            new HashMap<>();

    public void addBento(final @NotNull Bento bento) {
        BENTO_MAP.put(bento.getIdentifier(), bento);
    }

    @Override
    public @NotNull Optional<@NotNull Bento> getBento(@NotNull String identifier) {
        return Optional.ofNullable(BENTO_MAP.get(identifier));
    }

    @Override
    public @NotNull Collection<@NotNull Bento> getAllBentos() {
        return new ArrayList<>(BENTO_MAP.values());
    }
}
