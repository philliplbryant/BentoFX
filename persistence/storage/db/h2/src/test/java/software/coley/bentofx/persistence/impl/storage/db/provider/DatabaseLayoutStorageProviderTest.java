package software.coley.bentofx.persistence.impl.storage.db.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Coverage for the two {@code DatabaseLayoutStorageProviderIT} behaviors that
 * never reach the lazily-created {@code EntityManagerFactory}: identifying
 * itself, and rejecting a device-named layout identifier before the factory
 * is ever asked for. Everything else that provider does needs the real
 * database {@code DatabaseLayoutStorageProviderIT} stands up.
 */
class DatabaseLayoutStorageProviderTest {

    private static final String CODEC_IDENTIFIER = "json";
    private static final String STORAGE_IDENTIFIER = "h2";

    @Test
    void providerIdentifiesItselfAsH2() {
        assertThat(new DatabaseLayoutStorageProvider().getIdentifier())
                .describedAs("provider.getIdentifier()")
                .isEqualTo(STORAGE_IDENTIFIER);
    }

    @Test
    void appliesTheSharedIdentifierRule() {
        final DatabaseLayoutStorageProvider provider =
                new DatabaseLayoutStorageProvider();

        // A device name fits every column this storage has, so nothing here would
        // reject it: this passes only if the provider applies the shared identifier
        // rule as well. It rejects before the factory is created, so no database
        // is needed to exercise it.
        assertThatThrownBy(() -> provider.getLayoutStorage("nul", CODEC_IDENTIFIER))
                .describedAs("layout identifier naming a device")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("device");
    }
}
