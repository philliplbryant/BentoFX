package software.coley.bentofx.persistence.impl.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.api.LayoutRestorer;
import software.coley.bentofx.persistence.api.LayoutSaver;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.testfixtures.provider.TestLayoutCodecProvider;
import software.coley.bentofx.persistence.testfixtures.provider.TestLayoutStorageProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDockingLayoutPersistenceProviderTest {

    private static final String DEFAULT_LAYOUT_IDENTIFIER = "default";
    private static final String JSON_CODEC_IDENTIFIER = "json";
    private static final String XML_CODEC_IDENTIFIER = "xml";
    private static final String FILE_STORAGE_IDENTIFIER = "file";
    private static final String DATABASE_STORAGE_IDENTIFIER = "h2";

    private static final String DATABASEPROVIDER_GET_LAYOUTIDENTIFIER_DESCRIPTION = "databaseProvider.getLayoutIdentifier()";
    private static final String EXCEPTION_THROWN_BY_PROVIDER_GET_DESCRIPTION =
            "exception thrown by () -> provider.getLayoutSaver(\"default\", new DefaultBentoProvider())";
    private static final String FILEPROVIDER_GET_LAYOUTIDENTIFIER_DESCRIPTION = "fileProvider.getLayoutIdentifier()";
    private static final String JSONPROVIDER_GET_CREATEDCODECCOUNT_DESCRIPTION = "jsonProvider.getCreatedCodecCount()";
    private static final String XMLPROVIDER_GET_CREATEDCODECCOUNT_DESCRIPTION = "xmlProvider.getCreatedCodecCount()";

    @Test
    void usesSingleCodecAndStorageProvidersWithoutExplicitSelection() throws BentoStateException {
        final TestLayoutCodecProvider codecProvider = new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false);
        final TestLayoutStorageProvider storageProvider = new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider),
                        List.of(storageProvider)
                );

        provider.getLayoutSaver(DEFAULT_LAYOUT_IDENTIFIER, new DefaultBentoProvider());

        assertThat(codecProvider.getCreatedCodecCount())
                .describedAs("codecProvider.getCreatedCodecCount()")
                .isEqualTo(1);
        assertThat(storageProvider.getLayoutIdentifier())
                .describedAs("storageProvider.getLayoutIdentifier()")
                .isEqualTo(DEFAULT_LAYOUT_IDENTIFIER);
        assertThat(storageProvider.getCodecIdentifier())
                .describedAs("storageProvider.getCodecIdentifier()")
                .isEqualTo(JSON_CODEC_IDENTIFIER);
    }

    @Test
    void usesExplicitProviderIdentifiersWhenMultipleProvidersAreAvailable() throws BentoStateException {
        final TestLayoutCodecProvider jsonProvider = new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false);
        final TestLayoutCodecProvider xmlProvider = new TestLayoutCodecProvider(XML_CODEC_IDENTIFIER, false);
        final TestLayoutStorageProvider fileProvider = new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false);
        final TestLayoutStorageProvider databaseProvider = new TestLayoutStorageProvider(DATABASE_STORAGE_IDENTIFIER, false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(jsonProvider, xmlProvider),
                        List.of(fileProvider, databaseProvider)
                );

        provider.getLayoutSaver(
                new LayoutPersistenceProfile(DEFAULT_LAYOUT_IDENTIFIER, XML_CODEC_IDENTIFIER, DATABASE_STORAGE_IDENTIFIER),
                new DefaultBentoProvider()
        );

        assertThat(jsonProvider.getCreatedCodecCount())
                .describedAs(JSONPROVIDER_GET_CREATEDCODECCOUNT_DESCRIPTION)
                .isZero();
        assertThat(xmlProvider.getCreatedCodecCount())
                .describedAs(XMLPROVIDER_GET_CREATEDCODECCOUNT_DESCRIPTION)
                .isEqualTo(1);
        assertThat(fileProvider.getLayoutIdentifier())
                .describedAs(FILEPROVIDER_GET_LAYOUTIDENTIFIER_DESCRIPTION)
                .isNull();
        assertThat(databaseProvider.getLayoutIdentifier())
                .describedAs(DATABASEPROVIDER_GET_LAYOUTIDENTIFIER_DESCRIPTION)
                .isEqualTo(DEFAULT_LAYOUT_IDENTIFIER);
        assertThat(databaseProvider.getCodecIdentifier())
                .describedAs("databaseProvider.getCodecIdentifier()")
                .isEqualTo(XML_CODEC_IDENTIFIER);
    }

    @Test
    void usesSingleDefaultProviderWhenMultipleProvidersAreAvailableWithoutExplicitSelection() throws BentoStateException {
        final TestLayoutCodecProvider jsonProvider = new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false);
        final TestLayoutCodecProvider xmlProvider = new TestLayoutCodecProvider(XML_CODEC_IDENTIFIER, true);
        final TestLayoutStorageProvider fileProvider = new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, true);
        final TestLayoutStorageProvider databaseProvider = new TestLayoutStorageProvider(DATABASE_STORAGE_IDENTIFIER, false);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(jsonProvider, xmlProvider),
                        List.of(fileProvider, databaseProvider)
                );

        provider.getLayoutSaver(DEFAULT_LAYOUT_IDENTIFIER, new DefaultBentoProvider());

        assertThat(jsonProvider.getCreatedCodecCount())
                .describedAs(JSONPROVIDER_GET_CREATEDCODECCOUNT_DESCRIPTION)
                .isZero();
        assertThat(xmlProvider.getCreatedCodecCount())
                .describedAs(XMLPROVIDER_GET_CREATEDCODECCOUNT_DESCRIPTION)
                .isEqualTo(1);
        assertThat(fileProvider.getLayoutIdentifier())
                .describedAs(FILEPROVIDER_GET_LAYOUTIDENTIFIER_DESCRIPTION)
                .isEqualTo(DEFAULT_LAYOUT_IDENTIFIER);
        assertThat(fileProvider.getCodecIdentifier())
                .describedAs("fileProvider.getCodecIdentifier()")
                .isEqualTo(XML_CODEC_IDENTIFIER);
        assertThat(databaseProvider.getLayoutIdentifier())
                .describedAs(DATABASEPROVIDER_GET_LAYOUTIDENTIFIER_DESCRIPTION)
                .isNull();
    }


    @Test
    void providerCreatedSaverClosesLayoutStorage() throws BentoStateException {
        final TestLayoutCodecProvider codecProvider =
                new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false);
        final CloseTrackingLayoutStorageProvider storageProvider =
                new CloseTrackingLayoutStorageProvider(FILE_STORAGE_IDENTIFIER);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider),
                        List.of(storageProvider)
                );

        final LayoutSaver layoutSaver =
                provider.getLayoutSaver(DEFAULT_LAYOUT_IDENTIFIER, new DefaultBentoProvider());
        layoutSaver.close();
        layoutSaver.close();

        assertThat(storageProvider.getLayoutStorage().getCloseCount())
                .describedAs("storageProvider.getLayoutStorage().getCloseCount()")
                .isEqualTo(1);
    }

    @Test
    void providerCreatedRestorerClosesLayoutStorage() throws BentoStateException {
        final TestLayoutCodecProvider codecProvider =
                new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false);
        final CloseTrackingLayoutStorageProvider storageProvider =
                new CloseTrackingLayoutStorageProvider(FILE_STORAGE_IDENTIFIER);

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider),
                        List.of(storageProvider)
                );

        final LayoutRestorer layoutRestorer =
                provider.getLayoutRestorer(
                        DEFAULT_LAYOUT_IDENTIFIER,
                        new DefaultBentoProvider(),
                        actualId -> Optional.empty(),
                        null,
                        null
                );
        layoutRestorer.close();
        layoutRestorer.close();

        assertThat(storageProvider.getLayoutStorage().getCloseCount())
                .describedAs("storageProvider.getLayoutStorage().getCloseCount()")
                .isEqualTo(1);
    }

    @Test
    void failsWhenMultipleProvidersAreAvailableWithoutExplicitSelectionOrDefault() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(
                                new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false),
                                new TestLayoutCodecProvider(XML_CODEC_IDENTIFIER, false)
                        ),
                        List.of(new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver(DEFAULT_LAYOUT_IDENTIFIER, new DefaultBentoProvider()))
                .describedAs(EXCEPTION_THROWN_BY_PROVIDER_GET_DESCRIPTION)
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Multiple LayoutCodecProvider implementations")
                .hasMessageContaining(JSON_CODEC_IDENTIFIER)
                .hasMessageContaining(XML_CODEC_IDENTIFIER);
    }

    @Test
    void failsWhenExplicitProviderIdentifierIsUnavailable() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false)),
                        List.of(new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver(
                new LayoutPersistenceProfile(DEFAULT_LAYOUT_IDENTIFIER, XML_CODEC_IDENTIFIER, FILE_STORAGE_IDENTIFIER),
                new DefaultBentoProvider()
        ))
                .describedAs("exception thrown by () -> provider.getLayoutSaver( new LayoutPersistenceProfile(\"defaul...")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining(XML_CODEC_IDENTIFIER)
                .hasMessageContaining(JSON_CODEC_IDENTIFIER);
    }

    @Test
    void failsWhenNoCodecProvidersAreAvailable() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(),
                        List.of(new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver(DEFAULT_LAYOUT_IDENTIFIER, new DefaultBentoProvider()))
                .describedAs(EXCEPTION_THROWN_BY_PROVIDER_GET_DESCRIPTION)
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("No LayoutCodecProvider implementation was found");
    }

    @Test
    void failsWhenMultipleDefaultCodecProvidersAreAvailable() {
        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(
                                new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, true),
                                new TestLayoutCodecProvider(XML_CODEC_IDENTIFIER, true)
                        ),
                        List.of(new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false))
                );

        assertThatThrownBy(() -> provider.getLayoutSaver(DEFAULT_LAYOUT_IDENTIFIER, new DefaultBentoProvider()))
                .describedAs(EXCEPTION_THROWN_BY_PROVIDER_GET_DESCRIPTION)
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining("Multiple default LayoutCodecProvider implementations")
                .hasMessageContaining(JSON_CODEC_IDENTIFIER)
                .hasMessageContaining(XML_CODEC_IDENTIFIER);
    }


    private static final class CloseTrackingLayoutStorageProvider implements LayoutStorageProvider {
        private final String identifier;
        private final CloseTrackingLayoutStorage layoutStorage =
                new CloseTrackingLayoutStorage();

        private CloseTrackingLayoutStorageProvider(final String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public LayoutStorage getLayoutStorage(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            return layoutStorage;
        }

        private CloseTrackingLayoutStorage getLayoutStorage() {
            return layoutStorage;
        }
    }

    private static final class CloseTrackingLayoutStorage implements LayoutStorage {
        private int closeCount;

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

        @Override
        public void close() {
            closeCount++;
        }

        private int getCloseCount() {
            return closeCount;
        }
    }
}
