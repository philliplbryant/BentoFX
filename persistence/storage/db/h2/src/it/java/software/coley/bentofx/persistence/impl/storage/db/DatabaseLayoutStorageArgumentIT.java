package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseLayoutStorageArgumentIT {

    private static final String LAYOUT_IDENTIFIER = "layout";
    private static final String CODEC_IDENTIFIER = "json";

    @TempDir
    private static @Nullable Path temporaryDirectory;

    private static @Nullable EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void setUpAll() {
        entityManagerFactory = Persistence.createEntityManagerFactory(
                "bentoLayout",
                Map.of(
                        "jakarta.persistence.jdbc.url",
                        "jdbc:h2:file:" + temporaryDirectory.resolve("db")
                                .toAbsolutePath()
                                .toString()
                                .replace(java.io.File.separatorChar, '/')
                                + ";DB_CLOSE_DELAY=-1"
                )
        );
    }

    @AfterAll
    static void tearDownAll() {
        entityManagerFactory.close();
    }

    @Test
    void rejectsAMissingEntityManagerFactory() {
        assertThatThrownBy(() ->
                new DatabaseLayoutStorage(null, LAYOUT_IDENTIFIER, CODEC_IDENTIFIER)
        )
                .describedAs("null entity manager factory")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("emf");
    }

    @Test
    void rejectsAMissingLayoutIdentifier() {
        assertThatThrownBy(() ->
                new DatabaseLayoutStorage(entityManagerFactory, null, CODEC_IDENTIFIER)
        )
                .describedAs("null layout identifier")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("layoutIdentifier");
    }

    @Test
    void rejectsAMissingCodecIdentifier() {
        assertThatThrownBy(() ->
                new DatabaseLayoutStorage(entityManagerFactory, LAYOUT_IDENTIFIER, null)
        )
                .describedAs("null codec identifier")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("codecIdentifier");
    }
}
