package software.coley.bentofx.persistence.impl.storage.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.impl.storage.file.FileLayoutStorage;

import java.io.File;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class FileLayoutStorageProviderTest {

    private static final String FILE_STORAGE_IDENTIFIER = "file";
    private static final String MAIN_LAYOUT_IDENTIFIER = "main-layout";
    private static final String USER_HOME_PROPERTY = "user.home";

    @Test
    void exposesFileIdentifierAndIsDefault() {
        final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

        assertThat(provider.getIdentifier())
                .describedAs("provider.getIdentifier()")
                .isEqualTo(FILE_STORAGE_IDENTIFIER);
        assertThat(provider.isDefault())
                .describedAs("provider.isDefault()")
                .isTrue();
    }

    @Test
    void createsFileStorageUsingLayoutIdentifierAndNormalizedCodecExtension() throws Exception {
        final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

        final Object storage = provider.getLayoutStorage(MAIN_LAYOUT_IDENTIFIER, ".json");

        assertThat(storage)
                .describedAs("storage")
                .isInstanceOf(FileLayoutStorage.class);
        assertThat(layoutFile((FileLayoutStorage) storage))
                .describedAs("layoutFile((FileLayoutStorage) storage)")
                .isEqualTo(new File(System.getProperty(USER_HOME_PROPERTY), ".bentofx/main-layout.json"));
    }

    @Test
    void createsFileStorageWithoutAddingDuplicateExtensionSeparator() throws Exception {
        final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

        final FileLayoutStorage storage = (FileLayoutStorage) provider.getLayoutStorage(
                MAIN_LAYOUT_IDENTIFIER,
                "xml"
        );

        assertThat(layoutFile(storage))
                .describedAs("layoutFile(storage)")
                .isEqualTo(new File(System.getProperty(USER_HOME_PROPERTY), ".bentofx/main-layout.xml"));
    }

    private static File layoutFile(final FileLayoutStorage storage) throws Exception {
        final Field fileField = FileLayoutStorage.class.getDeclaredField(FILE_STORAGE_IDENTIFIER);
        fileField.setAccessible(true);
        return (File) fileField.get(storage);
    }
}
