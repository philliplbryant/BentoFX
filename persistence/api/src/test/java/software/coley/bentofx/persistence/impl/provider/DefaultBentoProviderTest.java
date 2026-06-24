package software.coley.bentofx.persistence.impl.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.Bento;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultBentoProviderTest {

    @Test
    void constructorRegistersProvidedBentosByIdentifier() {
        final Bento first = new Bento("first");
        final Bento second = new Bento("second");
        final DefaultBentoProvider provider = new DefaultBentoProvider(first, second);

        assertThat(provider.getBento("first"))
                .containsSame(first);
        assertThat(provider.getBento("second"))
                .containsSame(second);
        assertThat(provider.getAllBentos())
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void addBentoReplacesExistingBentoWithSameIdentifier() {
        final Bento original = new Bento("duplicate");
        final Bento replacement = new Bento("duplicate");
        final DefaultBentoProvider provider = new DefaultBentoProvider(original);

        provider.addBento(replacement);

        assertThat(provider.getBento("duplicate"))
                .containsSame(replacement);
        assertThat(provider.getAllBentos())
                .containsExactly(replacement);
    }

    @Test
    void unknownIdentifierReturnsEmptyOptional() {
        final DefaultBentoProvider provider = new DefaultBentoProvider();

        assertThat(provider.getBento("missing"))
                .isEmpty();
    }
}
