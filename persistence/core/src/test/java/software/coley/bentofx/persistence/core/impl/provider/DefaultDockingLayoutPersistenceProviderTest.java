package software.coley.bentofx.persistence.core.impl.provider;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.core.api.BentoStateException;
import software.coley.bentofx.persistence.core.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.core.api.LayoutRestorer;
import software.coley.bentofx.persistence.core.api.LayoutSaver;
import software.coley.bentofx.persistence.core.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.core.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.testfixtures.provider.TestLayoutCodecProvider;
import software.coley.bentofx.persistence.testfixtures.provider.TestLayoutStorageProvider;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@NullMarked
class DefaultDockingLayoutPersistenceProviderTest {

    private static final String DEFAULT_LAYOUT_IDENTIFIER = "default";
    private static final String JSON_CODEC_IDENTIFIER = "json";
    private static final String XML_CODEC_IDENTIFIER = "xml";
    private static final String FILE_STORAGE_IDENTIFIER = "file";
    private static final String DATABASE_STORAGE_IDENTIFIER = "h2";

    private static final String DATABASE_PROVIDER_GET_LAYOUT_IDENTIFIER_DESCRIPTION =
            "databaseProvider.getLayoutIdentifier()";
    private static final String EXCEPTION_THROWN_BY_PROVIDER_GET_DESCRIPTION =
            "exception thrown by () -> provider.getLayoutSaver(\"default\", new DefaultBentoProvider())";
    private static final String FILE_PROVIDER_GET_LAYOUT_IDENTIFIER_DESCRIPTION =
            "fileProvider.getLayoutIdentifier()";
    private static final String JSON_PROVIDER_GET_CREATED_CODEC_COUNT_DESCRIPTION =
            "jsonProvider.getCreatedCodecCount()";
    private static final String XML_PROVIDER_GET_CREATED_CODEC_COUNT_DESCRIPTION =
            "xmlProvider.getCreatedCodecCount()";
    private static final String STORAGE_PROVIDER_GET_CREATED_LAYOUT_STORAGES_DESCRIPTION =
            "storageProvider.getCreatedLayoutStorages()";

    @Test
    void listsStoredLayoutsFromTheSelectedStorageProvider() throws BentoStateException {
        final TestLayoutCodecProvider codecProvider =
                new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false);
        final TestLayoutStorageProvider storageProvider =
                new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false);
        storageProvider.setStoredLayoutIdentifiers(
                List.of("compact", "multi-monitor")
        );

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider),
                        List.of(storageProvider)
                );

        assertThat(provider.getStoredLayoutIdentifiers(
                LayoutPersistenceProfile.of(DEFAULT_LAYOUT_IDENTIFIER)
        ))
                .describedAs("layouts the selected storage provider holds")
                .containsExactly("compact", "multi-monitor");
        assertThat(storageProvider.getCatalogCodecIdentifier())
                .describedAs("the codec identifier the catalog was asked with")
                .isEqualTo(JSON_CODEC_IDENTIFIER);
    }

    @Test
    void listsStoredLayoutsAsProfilesCarryingTheProfilesCodecAndStorage()
            throws BentoStateException {

        final TestLayoutStorageProvider storageProvider =
                new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false);
        storageProvider.setStoredLayoutIdentifiers(
                List.of("compact", "multi-monitor")
        );

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(new TestLayoutCodecProvider(
                                JSON_CODEC_IDENTIFIER, false
                        )),
                        List.of(storageProvider)
                );

        final LayoutPersistenceProfile profile = new LayoutPersistenceProfile(
                DEFAULT_LAYOUT_IDENTIFIER,
                JSON_CODEC_IDENTIFIER,
                FILE_STORAGE_IDENTIFIER
        );

        assertThat(provider.getStoredLayouts(profile))
                .describedAs("stored layouts as profiles")
                .extracting(
                        LayoutPersistenceProfile::layoutIdentifier,
                        LayoutPersistenceProfile::codecIdentifier,
                        LayoutPersistenceProfile::storageIdentifier
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                "compact",
                                JSON_CODEC_IDENTIFIER,
                                FILE_STORAGE_IDENTIFIER
                        ),
                        tuple(
                                "multi-monitor",
                                JSON_CODEC_IDENTIFIER,
                                FILE_STORAGE_IDENTIFIER
                        )
                );
    }

    @Test
    void reportsAndDeletesOneStoredLayout() throws BentoStateException {
        final TestLayoutStorageProvider storageProvider =
                new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false);
        storageProvider.setStoredLayoutIdentifiers(List.of("compact"));

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false)),
                        List.of(storageProvider)
                );

        final LayoutPersistenceProfile profile =
                LayoutPersistenceProfile.of("compact");

        assertThat(provider.isLayoutStored(profile))
                .describedAs("isLayoutStored for a layout the destination holds")
                .isTrue();
        assertThat(provider.deleteLayout(profile))
                .describedAs("deleteLayout for a layout the destination holds")
                .isTrue();
        assertThat(provider.isLayoutStored(profile))
                .describedAs("isLayoutStored after the delete")
                .isFalse();
        assertThat(provider.deleteLayout(profile))
                .describedAs("deleteLayout for a layout that is already gone")
                .isFalse();
    }

    @Test
    void catalogHonorsTheProfilesStorageIdentifier() throws BentoStateException {
        final TestLayoutStorageProvider fileProvider =
                new TestLayoutStorageProvider(FILE_STORAGE_IDENTIFIER, false);
        final TestLayoutStorageProvider databaseProvider =
                new TestLayoutStorageProvider(DATABASE_STORAGE_IDENTIFIER, false);
        databaseProvider.setStoredLayoutIdentifiers(List.of("in-the-database"));

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false)),
                        List.of(fileProvider, databaseProvider)
                );

        assertThat(provider.getStoredLayoutIdentifiers(new LayoutPersistenceProfile(
                DEFAULT_LAYOUT_IDENTIFIER,
                JSON_CODEC_IDENTIFIER,
                DATABASE_STORAGE_IDENTIFIER
        )))
                .describedAs("layouts held by the storage the profile names")
                .containsExactly("in-the-database");
        assertThat(fileProvider.getCatalogCodecIdentifier())
                .describedAs("the storage provider the profile did not name")
                .isNull();
    }

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
                .describedAs(JSON_PROVIDER_GET_CREATED_CODEC_COUNT_DESCRIPTION)
                .isZero();
        assertThat(xmlProvider.getCreatedCodecCount())
                .describedAs(XML_PROVIDER_GET_CREATED_CODEC_COUNT_DESCRIPTION)
                .isEqualTo(1);
        assertThat(fileProvider.getLayoutIdentifier())
                .describedAs(FILE_PROVIDER_GET_LAYOUT_IDENTIFIER_DESCRIPTION)
                .isNull();
        assertThat(databaseProvider.getLayoutIdentifier())
                .describedAs(DATABASE_PROVIDER_GET_LAYOUT_IDENTIFIER_DESCRIPTION)
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
                .describedAs(JSON_PROVIDER_GET_CREATED_CODEC_COUNT_DESCRIPTION)
                .isZero();
        assertThat(xmlProvider.getCreatedCodecCount())
                .describedAs(XML_PROVIDER_GET_CREATED_CODEC_COUNT_DESCRIPTION)
                .isEqualTo(1);
        assertThat(fileProvider.getLayoutIdentifier())
                .describedAs(FILE_PROVIDER_GET_LAYOUT_IDENTIFIER_DESCRIPTION)
                .isEqualTo(DEFAULT_LAYOUT_IDENTIFIER);
        assertThat(fileProvider.getCodecIdentifier())
                .describedAs("fileProvider.getCodecIdentifier()")
                .isEqualTo(XML_CODEC_IDENTIFIER);
        assertThat(databaseProvider.getLayoutIdentifier())
                .describedAs(DATABASE_PROVIDER_GET_LAYOUT_IDENTIFIER_DESCRIPTION)
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

        assertThat(storageProvider.getCreatedLayoutStorages())
                .describedAs(STORAGE_PROVIDER_GET_CREATED_LAYOUT_STORAGES_DESCRIPTION)
                .singleElement()
                .extracting(CloseTrackingLayoutStorage::getCloseCount)
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

        assertThat(storageProvider.getCreatedLayoutStorages())
                .describedAs(STORAGE_PROVIDER_GET_CREATED_LAYOUT_STORAGES_DESCRIPTION)
                .singleElement()
                .extracting(CloseTrackingLayoutStorage::getCloseCount)
                .isEqualTo(1);
    }

    @Test
    void closingAProviderCreatedSaverLeavesTheRestorersStorageOpen() throws BentoStateException {
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
        provider.getLayoutRestorer(
                DEFAULT_LAYOUT_IDENTIFIER,
                new DefaultBentoProvider(),
                actualId -> Optional.empty(),
                null,
                null
        );

        layoutSaver.close();

        // A storage each, and closing the saver must not reach the restorer's.
        // Collapsing the provider's two getLayoutStorage calls into one shared
        // instance is what this guards against: a saver that outlives nothing
        // would still shut the storage the restorer reads from.
        final List<CloseTrackingLayoutStorage> createdLayoutStorages =
                storageProvider.getCreatedLayoutStorages();
        assertThat(createdLayoutStorages)
                .describedAs(STORAGE_PROVIDER_GET_CREATED_LAYOUT_STORAGES_DESCRIPTION)
                .hasSize(2);
        assertThat(createdLayoutStorages.get(1).getCloseCount())
                .describedAs("close count of the restorer's storage")
                .isZero();
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

    /**
     * The no-args constructor discovers providers with {@code ServiceLoader}
     * rather than taking them explicitly. This module declares no {@code
     * provides} clause of its own, so discovery legitimately finds nothing -
     * what this covers is that construction itself succeeds either way.
     */
    @Test
    void noArgsConstructorDiscoversProvidersViaServiceLoaderWithoutThrowing() {
        assertThatCode(DefaultDockingLayoutPersistenceProvider::new)
                .describedAs("constructing DefaultDockingLayoutPersistenceProvider() with no explicit providers")
                .doesNotThrowAnyException();
    }

    @Test
    void getStoredLayoutsWrapsAReadFailureAsBentoStateException() {
        final String layoutIdentifier = "unreadable";
        final IOException openFailure = new IOException("could not open");
        final FailingOpenLayoutStorageProvider storageProvider =
                new FailingOpenLayoutStorageProvider(
                        FILE_STORAGE_IDENTIFIER,
                        layoutIdentifier,
                        openFailure
                );

        final DefaultDockingLayoutPersistenceProvider provider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(new TestLayoutCodecProvider(JSON_CODEC_IDENTIFIER, false)),
                        List.of(storageProvider)
                );

        assertThatThrownBy(() -> provider.getStoredLayouts(
                new LayoutPersistenceProfile(
                        DEFAULT_LAYOUT_IDENTIFIER,
                        JSON_CODEC_IDENTIFIER,
                        FILE_STORAGE_IDENTIFIER
                )
        ))
                .describedAs("exception thrown by getStoredLayouts when reading a layout's display name fails")
                .isInstanceOf(BentoStateException.class)
                .hasMessageContaining(layoutIdentifier)
                .hasCause(openFailure);
    }

    /**
     * Reports one stored layout, whose storage fails to open for reading -
     * the way an entry could disappear, or become unreadable, between being
     * listed and being read.
     */
    private static final class FailingOpenLayoutStorageProvider
            extends software.coley.bentofx.persistence.testfixtures.provider.AbstractTestLayoutProvider
            implements LayoutStorageProvider {

        private final String layoutIdentifier;
        private final IOException openFailure;

        private FailingOpenLayoutStorageProvider(
                final String identifier,
                final String layoutIdentifier,
                final IOException openFailure
        ) {
            super(identifier, false);
            this.layoutIdentifier = layoutIdentifier;
            this.openFailure = openFailure;
        }

        @Override
        public List<String> getLayoutIdentifiers(final String codecIdentifier) {
            return List.of(layoutIdentifier);
        }

        @Override
        public LayoutStorage getLayoutStorage(
                final String layoutIdentifier,
                final String codecIdentifier
        ) {
            return new LayoutStorage() {
                @Override
                public boolean exists() {
                    return true;
                }

                @Override
                public InputStream openInputStream() throws IOException {
                    throw openFailure;
                }

                @Override
                public OutputStream openOutputStream() {
                    return new ByteArrayOutputStream();
                }
            };
        }
    }


    /**
     * Hands out a fresh storage per call, the way a real
     * {@link LayoutStorageProvider} does, and keeps every instance it created so
     * a test can tell which component closed which.
     */
    private static final class CloseTrackingLayoutStorageProvider implements LayoutStorageProvider {
        private final String identifier;
        private final List<CloseTrackingLayoutStorage> createdLayoutStorages =
                new ArrayList<>();

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
            final CloseTrackingLayoutStorage layoutStorage =
                    new CloseTrackingLayoutStorage();
            createdLayoutStorages.add(layoutStorage);
            return layoutStorage;
        }

        private List<CloseTrackingLayoutStorage> getCreatedLayoutStorages() {
            return createdLayoutStorages;
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
