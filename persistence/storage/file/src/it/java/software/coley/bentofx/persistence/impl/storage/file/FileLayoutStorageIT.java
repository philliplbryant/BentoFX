package software.coley.bentofx.persistence.impl.storage.file;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileLayoutStorageIT {
	private static final String TEST_FILE_NAME = "test-layout.bento";
	private static final String TEST_FILE_CONTENT = "Test data for FileLayoutStorage integration test";

	private FileLayoutStorage fileLayoutStorage;
	private File testFile;

	@BeforeEach
	void setUp() throws IOException {
		// Create a temporary directory and file to work with
		final Path tempDir = Files.createTempDirectory("test-layout-storage");
		testFile = new File(tempDir.toFile(), TEST_FILE_NAME);
		fileLayoutStorage = new FileLayoutStorage(testFile);
	}

	@AfterEach
	void tearDown() throws IOException {
		// Delete test files and directories
		if (testFile.exists()) {
			Files.deleteIfExists(testFile.toPath());
		}

		// Delete the temp directory
		if (testFile.getParentFile().exists()) {
			Files.deleteIfExists(testFile.getParentFile().toPath());
		}
	}

	@Test
	void testFileLayoutStorageIntegration() throws IOException {
		// Test the file does not initially exist
		assertFalse(fileLayoutStorage.exists(), "File should not exist initially");

		// Write data to the file and verify existence
		try (OutputStream outputStream = fileLayoutStorage.openOutputStream()) {
			outputStream.write(TEST_FILE_CONTENT.getBytes());
		}
		assertTrue(fileLayoutStorage.exists(), "File should exist after writing to it");
		assertTrue(testFile.length() > 0, "File length should be greater than zero after writing data");

		// Read data from the file and verify the content
		StringBuilder fileContent = new StringBuilder();
		try (InputStream inputStream = fileLayoutStorage.openInputStream();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line);
			}
		}
		assertEquals(TEST_FILE_CONTENT, fileContent.toString(), "File content should match the written data");
	}
}
