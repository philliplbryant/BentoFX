package software.coley.bentofx.persistence.impl.storage.file.provider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.coley.bentofx.persistence.core.api.storage.LayoutStorage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests listing and deleting stored layouts against a real directory.
 */
class FileLayoutStorageCatalogIT {

	private static final String CODEC_IDENTIFIER = "json";
	private static final String OTHER_CODEC_IDENTIFIER = "xml";
	private static final String USER_HOME_PROPERTY = "user.home";
	private static final String LAYOUTS_DIRECTORY_PATH = ".bentofx/layouts";
	private static final String LAYOUT_CONTENT = "{}";

	@TempDir
	private static Path temporaryHome;

	private static Path bentoDirectory;

	/**
	 * Because the provider resolves its directory from {@code user.home} on
	 * every call, these tests direct {@code user.home} to a temporary
	 * directory before using the provider. Then tests to determine if the
	 * redirection succeeded by writing a layout through the provider and
	 * finding it there.
	 *
	 * @throws IOException when writing the probe layout fails.
	 */
	@BeforeAll
	static void redirectUserHomeAndProveItTook() throws IOException {
		System.setProperty(USER_HOME_PROPERTY, temporaryHome.toString());

		bentoDirectory = Files.createDirectories(
				temporaryHome.resolve(LAYOUTS_DIRECTORY_PATH)
		);

		final FileLayoutStorageProvider provider =
				new FileLayoutStorageProvider();

		try (final LayoutStorage storage =
				     provider.getLayoutStorage(
						     "redirect-probe",
						     CODEC_IDENTIFIER
				     );
		     final OutputStream outputStream = storage.openOutputStream()
		) {

			outputStream.write(LAYOUT_CONTENT.getBytes(UTF_8));
		}

		assertThat(bentoDirectory.resolve("redirect-probe." + CODEC_IDENTIFIER))
				.describedAs("a layout written through the provider")
				.exists();
	}

	@BeforeEach
	void emptyTheLayoutDirectory() throws IOException {
		try (final Stream<Path> entries = Files.list(bentoDirectory)) {
			for (final Path entry : entries.toList()) {
				deleteRecursively(entry);
			}
		}
	}

	@Test
	void listsOnlyTheLayoutsStoredForTheCodec() throws IOException {
		writeLayout("first.json");
		writeLayout("second.json");
		writeLayout("elsewhere.xml");

		final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

		assertThat(provider.getLayoutIdentifiers(CODEC_IDENTIFIER))
				.describedAs("layouts stored for the json codec")
				.containsExactlyInAnyOrder("first", "second");
		assertThat(provider.getLayoutIdentifiers(OTHER_CODEC_IDENTIFIER))
				.describedAs("layouts stored for the xml codec")
				.containsExactly("elsewhere");
	}

	@Test
	void skipsAnEmptyFileAndADirectory() throws IOException {
		writeLayout("real.json");
		Files.createFile(bentoDirectory.resolve("empty.json"));
		Files.createDirectories(bentoDirectory.resolve("directory.json"));

		assertThat(new FileLayoutStorageProvider().getLayoutIdentifiers(CODEC_IDENTIFIER))
				.describedAs("layouts stored, with an empty file and a directory present")
				.containsExactly("real");
	}

	@Test
	void reportsNoLayoutsForACodecNothingWasWrittenWith() throws IOException {
		writeLayout("only.json");

		assertThat(new FileLayoutStorageProvider().getLayoutIdentifiers("none"))
				.describedAs("layouts stored for a codec nothing was written with")
				.isEmpty();
	}

	@Test
	void deletesOneLayoutAndReportsWhetherThereWasOne() throws IOException {
		writeLayout("removable.json");

		final FileLayoutStorageProvider provider = new FileLayoutStorageProvider();

		assertThat(provider.isLayoutStored("removable", CODEC_IDENTIFIER))
				.describedAs("isLayoutStored before the delete")
				.isTrue();
		assertThat(provider.deleteLayout("removable", CODEC_IDENTIFIER))
				.describedAs("deleteLayout for a stored layout")
				.isTrue();
		assertThat(provider.isLayoutStored("removable", CODEC_IDENTIFIER))
				.describedAs("isLayoutStored after the delete")
				.isFalse();
		assertThat(provider.deleteLayout("removable", CODEC_IDENTIFIER))
				.describedAs("deleteLayout for a layout that is already gone")
				.isFalse();
	}

	/**
	 * A separate, never-redirected-to home directory, distinct from the
	 * class-level one every other test in this file shares: that one already
	 * has its layouts directory created by {@link #redirectUserHomeAndProveItTook()}.
	 */
	@Test
	void reportsNoLayoutsWhenTheLayoutsDirectoryDoesNotExistYet(
			@TempDir final Path freshHome
	) throws IOException {
		final String realUserHome = System.getProperty(USER_HOME_PROPERTY);
		System.setProperty(USER_HOME_PROPERTY, freshHome.toString());

		try {
			assertThat(new FileLayoutStorageProvider().getLayoutIdentifiers(CODEC_IDENTIFIER))
					.describedAs("layouts stored when the layouts directory does not exist yet")
					.isEmpty();
		} finally {
			System.setProperty(USER_HOME_PROPERTY, realUserHome);
		}
	}

	private static void writeLayout(final String fileName) throws IOException {
		Files.writeString(bentoDirectory.resolve(fileName), LAYOUT_CONTENT, UTF_8);
	}

	private static void deleteRecursively(final Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (final Stream<Path> children = Files.list(path)) {
				for (final Path child : children.toList()) {
					deleteRecursively(child);
				}
			}
		}

		Files.deleteIfExists(path);
	}
}
