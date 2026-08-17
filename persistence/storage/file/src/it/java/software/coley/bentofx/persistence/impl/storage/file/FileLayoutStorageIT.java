package software.coley.bentofx.persistence.impl.storage.file;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class FileLayoutStorageIT {
	private static final String TEST_FILE_NAME = "test-layout.bento";
	private static final String TEST_FILE_CONTENT = "Test data for FileLayoutStorage integration test";
	private static final String PREVIOUS_FILE_CONTENT = "A layout that was already saved";

	@TempDir
	private @Nullable Path temporaryDirectory;

	private @Nullable FileLayoutStorage fileLayoutStorage;
	private @Nullable File testFile;

	@BeforeEach
	void setUp() {
		testFile = new File(temporaryDirectory.toFile(), TEST_FILE_NAME);
		fileLayoutStorage = new FileLayoutStorage(testFile);
	}

	@Test
	void previousLayoutSurvivesUntilTheStreamCloses() throws IOException {
		Files.writeString(testFile.toPath(), PREVIOUS_FILE_CONTENT, UTF_8);

		try (OutputStream outputStream = fileLayoutStorage.openOutputStream()) {
			outputStream.write(TEST_FILE_CONTENT.getBytes(UTF_8));
			outputStream.flush();

			assertThat(Files.readString(testFile.toPath(), UTF_8))
					.describedAs("target content while the stream is open")
					.isEqualTo(PREVIOUS_FILE_CONTENT);
		}

		assertThat(Files.readString(testFile.toPath(), UTF_8))
				.describedAs("target content after the stream closes")
				.isEqualTo(TEST_FILE_CONTENT);
	}

	@Test
	void previousLayoutSurvivesAStreamThatIsNeverClosed() throws IOException {
		Files.writeString(testFile.toPath(), PREVIOUS_FILE_CONTENT, UTF_8);

		// A save that dies part way through, which used to leave the target
		// holding whatever had been written so far.
		final OutputStream abandoned = fileLayoutStorage.openOutputStream();
		abandoned.write(TEST_FILE_CONTENT.getBytes(UTF_8));
		abandoned.flush();

		assertThat(Files.readString(testFile.toPath(), UTF_8))
				.describedAs("target content after an abandoned save")
				.isEqualTo(PREVIOUS_FILE_CONTENT);
	}

	@Test
	void testFileLayoutStorageIntegration() throws IOException {
		// Test the file does not initially exist
		assertThat(fileLayoutStorage.exists())
                .describedAs("File should not exist initially")
                .isFalse();

		// Write data to the file and verify existence
		try (OutputStream outputStream = fileLayoutStorage.openOutputStream()) {
			outputStream.write(TEST_FILE_CONTENT.getBytes(UTF_8));
		}
		assertThat(fileLayoutStorage.exists())
                .describedAs("File should exist after writing to it")
                .isTrue();
		assertThat(testFile)
                .describedAs("File length should be greater than zero after writing data")
                .isNotEmpty();

		// Read data from the file and verify the content
		StringBuilder fileContent = new StringBuilder();
		try (InputStream inputStream = fileLayoutStorage.openInputStream();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line);
			}
		}
		assertThat(fileContent)
                .describedAs("File content should match the written data")
                .hasToString(TEST_FILE_CONTENT);
	}
}
