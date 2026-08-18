package software.coley.bentofx.persistence.impl.storage.db.provider;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the persistence unit the module ships, which the storage tests
 * replace with a temporary one. Everything the packaged unit decides: where the
 * database file goes, what credentials open it, how the table comes to exist.
 */
class DatabaseLayoutStorageProviderIT {

    private static final String USER_HOME_PROPERTY = "user.home";
    private static final String BENTO_DIRECTORY_NAME = ".bentofx";
    private static final String LAYOUT_IDENTIFIER = "provider-layout";
    private static final String CODEC_IDENTIFIER = "json";
    private static final String STORAGE_IDENTIFIER = "h2";
    private static final String TEST_DATA = "A layout stored through the provider.";

    @TempDir
    private @Nullable Path temporaryHome;

    private @Nullable String realUserHome;

    @BeforeEach
    void setUp() {
        realUserHome = System.getProperty(USER_HOME_PROPERTY);

        // The shipped JDBC URL is relative to the user's home, and Hibernate
        // resolves that placeholder from the system property when the factory is
        // created.
        System.setProperty(USER_HOME_PROPERTY, temporaryHome.toString());
    }

    @AfterEach
    void tearDown() {
        System.setProperty(USER_HOME_PROPERTY, realUserHome);
    }

    @Test
    void providerIdentifiesItselfAsH2() {
        assertThat(new DatabaseLayoutStorageProvider().getIdentifier())
                .describedAs("provider.getIdentifier()")
                .isEqualTo(STORAGE_IDENTIFIER);
    }

    @Test
    void storesAndReadsALayoutThroughTheShippedPersistenceUnit() throws IOException {
        final DatabaseLayoutStorageProvider provider =
                new DatabaseLayoutStorageProvider();

        try (LayoutStorage storage =
                     provider.getLayoutStorage(LAYOUT_IDENTIFIER, CODEC_IDENTIFIER)) {

            assertThat(storage.exists())
                    .describedAs("storage.exists() before anything is stored")
                    .isFalse();

            try (OutputStream outputStream = storage.openOutputStream()) {
                outputStream.write(TEST_DATA.getBytes(UTF_8));
            }

            assertThat(storage.exists())
                    .describedAs("storage.exists() after storing a layout")
                    .isTrue();

            try (InputStream inputStream = storage.openInputStream()) {
                assertThat(new String(inputStream.readAllBytes(), UTF_8))
                        .describedAs("stored layout read back")
                        .isEqualTo(TEST_DATA);
            }
        }

        assertThat(Files.isDirectory(temporaryHome.resolve(BENTO_DIRECTORY_NAME)))
                .describedAs("the database was created under the user's home")
                .isTrue();
    }

    @Test
    void listsAndDeletesStoredLayouts() throws IOException {
        final DatabaseLayoutStorageProvider provider =
                new DatabaseLayoutStorageProvider();

        storeLayout(provider, "first-layout");
        storeLayout(provider, "second-layout");

        assertThat(provider.getLayoutIdentifiers(CODEC_IDENTIFIER))
                .describedAs("layouts stored for this codec")
                .containsExactlyInAnyOrder("first-layout", "second-layout");
        assertThat(provider.getLayoutIdentifiers("none"))
                .describedAs("layouts stored for a codec nothing was written with")
                .isEmpty();

        assertThat(provider.isLayoutStored("first-layout", CODEC_IDENTIFIER))
                .describedAs("isLayoutStored before the delete")
                .isTrue();
        assertThat(provider.deleteLayout("first-layout", CODEC_IDENTIFIER))
                .describedAs("deleteLayout for a stored layout")
                .isTrue();
        assertThat(provider.deleteLayout("first-layout", CODEC_IDENTIFIER))
                .describedAs("deleteLayout for a layout that is already gone")
                .isFalse();

        assertThat(provider.getLayoutIdentifiers(CODEC_IDENTIFIER))
                .describedAs("layouts stored after the delete")
                .containsExactly("second-layout");
    }

    @Test
    void appliesTheSharedIdentifierRule() {
        final DatabaseLayoutStorageProvider provider =
                new DatabaseLayoutStorageProvider();

        // Nothing about a column width rejects this one, so it holds only if the
        // provider applies the rule the file storage applies.
        assertThatThrownBy(() -> provider.getLayoutStorage("nul", CODEC_IDENTIFIER))
                .describedAs("layout identifier naming a device")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("device");
    }

    /**
     * Writes a layout through the provider, so that the catalog reads rows the
     * provider's own storage produced.
     *
     * @param provider the provider to store through.
     * @param layoutIdentifier identifies the layout to store.
     * @throws IOException when the write fails.
     */
    private static void storeLayout(
            final DatabaseLayoutStorageProvider provider,
            final String layoutIdentifier
    ) throws IOException {
        try (final LayoutStorage storage =
                     provider.getLayoutStorage(layoutIdentifier, CODEC_IDENTIFIER);
             final OutputStream outputStream = storage.openOutputStream()) {

            outputStream.write(TEST_DATA.getBytes(UTF_8));
        }
    }

    @Test
    void handsOutAFreshStorageEachTime() {
        final DatabaseLayoutStorageProvider provider =
                new DatabaseLayoutStorageProvider();

        try (LayoutStorage first =
                     provider.getLayoutStorage(LAYOUT_IDENTIFIER, CODEC_IDENTIFIER);
             LayoutStorage second =
                     provider.getLayoutStorage(LAYOUT_IDENTIFIER, CODEC_IDENTIFIER)) {

            assertThat(first)
                    .describedAs("second storage for the same layout")
                    .isNotSameAs(second);
        }
    }
}
