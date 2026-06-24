package software.coley.bentofx.persistence.impl.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.testfixtures.provider.TestLayoutCodecProvider;
import software.coley.bentofx.persistence.testfixtures.provider.TestLayoutStorageProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDockingLayoutPersistenceProviderTest {

    @Test
    void usesSingleCodecAndStorageProvidersWithoutExplicitSelection() throws BentoStateException {
        final TestLayoutCodecProvider codecProvider = new TestLayoutCodecProvider("json", false);
        final TestLayoutStorageProvider storageProvider = new TestLayoutStorageProvider("file", false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider),
                        List.of(storageProvider)
                );

        provider.getLayoutSaver("default", new DefaultBentoProvider());

        assertThat(codecProvider.getCreatedCodecCount()).isEqualTo(1);
        assertThat(storageProvider.getLayoutIdentifier()).isEqualTo("default");
        assertThat(storageProvider.getCodecIdentifier()).isEqualTo("json");
    }

    @Test
    void usesExplicitProviderIdentifiersWhenMultipleProvidersAreAvailable() throws BentoStateException {
        final TestLayoutCodecProvider jsonProvider = new TestLayoutCodecProvider("json", false);
        final TestLayoutCodecProvider xmlProvider = new TestLayoutCodecProvider("xml", false);
        final TestLayoutStorageProvider fileProvider = new TestLayoutStorageProvider("file", false);
        final TestLayoutStorageProvider databaseProvider = new TestLayoutStorageProvider("h2", false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(jsonProvider, xmlProvider),
                        List.of(fileProvider, databaseProvider)
                );

        provider.getLayoutSaver(
                new LayoutPersistenceProfile("default", "xml", "h2"),
                new DefaultBentoProvider()
        );

        assertThat(jsonProvider.getCreatedCodecCount()).isZero();
        assertThat(xmlProvider.getCreatedCodecCount()).isEqualTo(1);
        assertThat(fileProvider.getLayoutIdentifier()).isNull();
        assertThat(databaseProvider.getLayoutIdentifier()).isEqualTo("default");
        assertThat(databaseProvider.getCodecIdentifier()).isEqualTo("xml");
    }

    @Test
    void usesSingleDefaultProviderWhenMultipleProvidersAreAvailableWithoutExplicitSelection() throws BentoStateException {
        final TestLayoutCodecProvider jsonProvider = new TestLayoutCodecProvider("json", false);
        final TestLayoutCodecProvider xmlProvider = new TestLayoutCodecProvider("xml", true);
        final TestLayoutStorageProvider fileProvider = new TestLayoutStorageProvider("file", true);
        final TestLayoutStorageProvider databaseProvider = new TestLayoutStorageProvider("h2", false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(jsonProvider, xmlProvider),
                        List.of(fileProvider, databaseProvider)
                );

        provider.getLayoutSaver("default", new DefaultBentoProvider());

        assertThat(jsonProvider.getCreatedCodecCount()).isZero();
        assertThat(xmlProvider.getCreatedCodecCount()).isEqualTo(1);
        assertThat(fileProvider.getLayoutIdentifier()).isEqualTo("default");
        assertThat(fileProvider.getCodecIdentifier()).isEqualTo("xml");
        assertThat(databaseProvider.getLayoutIdentifier()).isNull();
    }

    @Test
    void failsWhenMultipleProvidersAreAvailableWithoutExplicitSelectionOrDefault() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(
                                new TestLayoutCodecProvider("json", false),
                                new TestLayoutCodecProvider("xml", false)
                        ),
                        List.of(new TestLayoutStorageProvider("file", false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver("default", new DefaultBentoProvider()))
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Multiple LayoutCodecProvider implementations")
                .hasMessageContaining("json")
                .hasMessageContaining("xml");
    }

    @Test
    void failsWhenExplicitProviderIdentifierIsUnavailable() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(new TestLayoutCodecProvider("json", false)),
                        List.of(new TestLayoutStorageProvider("file", false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver(
                new LayoutPersistenceProfile("default", "xml", "file"),
                new DefaultBentoProvider()
        ))
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("xml")
                .hasMessageContaining("json");
    }

    @Test
    void failsWhenNoCodecProvidersAreAvailable() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(),
                        List.of(new TestLayoutStorageProvider("file", false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver("default", new DefaultBentoProvider()))
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("No LayoutCodecProvider implementation was found");
    }

    @Test
    void failsWhenMultipleDefaultCodecProvidersAreAvailable() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(
                                new TestLayoutCodecProvider("json", true),
                                new TestLayoutCodecProvider("xml", true)
                        ),
                        List.of(new TestLayoutStorageProvider("file", false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver("default", new DefaultBentoProvider()))
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Multiple default LayoutCodecProvider implementations")
                .hasMessageContaining("json")
                .hasMessageContaining("xml");
    }
}
