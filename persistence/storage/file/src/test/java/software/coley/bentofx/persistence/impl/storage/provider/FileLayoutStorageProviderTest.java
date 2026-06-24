package software.coley.bentofx.persistence.impl.storage.provider;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.impl.storage.file.FileLayoutStorage;

import java.io.File;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class FileLayoutStorageProviderTest {

    @Test
    void exposesFileIdentifierAndIsDefault() {
        final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

        assertThat(provider.getIdentifier())
                .isEqualTo("file");
        assertThat(provider.isDefault())
                .isTrue();
    }

    @Test
    void createsFileStorageUsingLayoutIdentifierAndNormalizedCodecExtension() throws Exception {
        final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

        final Object storage = provider.getLayoutStorage("main-layout", ".json");

        assertThat(storage)
                .isInstanceOf(FileLayoutStorage.class);
        assertThat(layoutFile((FileLayoutStorage) storage))
                .isEqualTo(new File(System.getProperty("user.home"), ".bentofx/main-layout.json"));
    }

    @Test
    void createsFileStorageWithoutAddingDuplicateExtensionSeparator() throws Exception {
        final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

        final FileLayoutStorage storage = (FileLayoutStorage) provider.getLayoutStorage(
                "main-layout",
                "xml"
        );

        assertThat(layoutFile(storage))
                .isEqualTo(new File(System.getProperty("user.home"), ".bentofx/main-layout.xml"));
    }

    private static File layoutFile(final FileLayoutStorage storage) throws Exception {
        final Field fileField = FileLayoutStorage.class.getDeclaredField("file");
        fileField.setAccessible(true);
        return (File) fileField.get(storage);
    }
}
