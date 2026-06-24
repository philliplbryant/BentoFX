package software.coley.bentofx.persistence.impl.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDockingLayoutPersistenceProviderTest {

    @Test
    void usesSingleCodecAndStorageProvidersWithoutExplicitSelection() throws BentoStateException {
        final TestCodecProvider codecProvider = new TestCodecProvider("json", false);
        final TestStorageProvider storageProvider = new TestStorageProvider("file", false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider),
                        List.of(storageProvider)
                );

        provider.getLayoutSaver("default", new DefaultBentoProvider());

        assertThat(codecProvider.createdCodecCount).isEqualTo(1);
        assertThat(storageProvider.layoutIdentifier).isEqualTo("default");
        assertThat(storageProvider.codecIdentifier).isEqualTo("json");
    }

    @Test
    void usesExplicitProviderIdentifiersWhenMultipleProvidersAreAvailable() throws BentoStateException {
        final TestCodecProvider jsonProvider = new TestCodecProvider("json", false);
        final TestCodecProvider xmlProvider = new TestCodecProvider("xml", false);
        final TestStorageProvider fileProvider = new TestStorageProvider("file", false);
        final TestStorageProvider databaseProvider = new TestStorageProvider("h2", false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(jsonProvider, xmlProvider),
                        List.of(fileProvider, databaseProvider)
                );

        provider.getLayoutSaver(
                new LayoutPersistenceProfile("default", "xml", "h2"),
                new DefaultBentoProvider()
        );

        assertThat(jsonProvider.createdCodecCount).isZero();
        assertThat(xmlProvider.createdCodecCount).isEqualTo(1);
        assertThat(fileProvider.layoutIdentifier).isNull();
        assertThat(databaseProvider.layoutIdentifier).isEqualTo("default");
        assertThat(databaseProvider.codecIdentifier).isEqualTo("xml");
    }

    @Test
    void usesSingleDefaultProviderWhenMultipleProvidersAreAvailableWithoutExplicitSelection() throws BentoStateException {
        final TestCodecProvider jsonProvider = new TestCodecProvider("json", false);
        final TestCodecProvider xmlProvider = new TestCodecProvider("xml", true);
        final TestStorageProvider fileProvider = new TestStorageProvider("file", true);
        final TestStorageProvider databaseProvider = new TestStorageProvider("h2", false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(jsonProvider, xmlProvider),
                        List.of(fileProvider, databaseProvider)
                );

        provider.getLayoutSaver("default", new DefaultBentoProvider());

        assertThat(jsonProvider.createdCodecCount).isZero();
        assertThat(xmlProvider.createdCodecCount).isEqualTo(1);
        assertThat(fileProvider.layoutIdentifier).isEqualTo("default");
        assertThat(fileProvider.codecIdentifier).isEqualTo("xml");
        assertThat(databaseProvider.layoutIdentifier).isNull();
    }

    @Test
    void failsWhenMultipleProvidersAreAvailableWithoutExplicitSelectionOrDefault() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(
                                new TestCodecProvider("json", false),
                                new TestCodecProvider("xml", false)
                        ),
                        List.of(new TestStorageProvider("file", false))
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
                        List.of(new TestCodecProvider("json", false)),
                        List.of(new TestStorageProvider("file", false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver(
                new LayoutPersistenceProfile("default", "xml", "file"),
                new DefaultBentoProvider()
        ))
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("xml")
                .hasMessageContaining("json");
    }

    private static final class TestCodecProvider implements LayoutCodecProvider {
        private final String identifier;
        private final boolean defaultProvider;
        private int createdCodecCount;

        private TestCodecProvider(
                final String identifier,
                final boolean defaultProvider
        ) {
            this.identifier = identifier;
            this.defaultProvider = defaultProvider;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public boolean isDefault() {
            return defaultProvider;
        }

        @Override
        public LayoutCodec getLayoutCodec() {
            createdCodecCount++;
            return new TestCodec(identifier);
        }
    }

    private static final class TestStorageProvider implements LayoutStorageProvider {
        private final String identifier;
        private final boolean defaultProvider;
        private String layoutIdentifier;
        private String codecIdentifier;

        private TestStorageProvider(
                final String identifier,
                final boolean defaultProvider
        ) {
            this.identifier = identifier;
            this.defaultProvider = defaultProvider;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public boolean isDefault() {
            return defaultProvider;
        }

        @Override
        public LayoutStorage getLayoutStorage(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            this.layoutIdentifier = layoutIdentifier;
            this.codecIdentifier = codecIdentifier;
            return new TestStorage();
        }
    }

    private record TestCodec(String identifier) implements LayoutCodec {
        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public void encode(
                final List<BentoState> bentoStates,
                final OutputStream outputStream
        ) throws BentoStateException {
            // no-op
        }

        @Override
        public List<BentoState> decode(
                final InputStream inputStream
        ) throws BentoStateException {
            return List.of();
        }
    }

    private static final class TestStorage implements LayoutStorage {
        @Override
        public boolean exists() {
            return false;
        }

        @Override
        public OutputStream openOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(new byte[0]);
        }
    }
}
