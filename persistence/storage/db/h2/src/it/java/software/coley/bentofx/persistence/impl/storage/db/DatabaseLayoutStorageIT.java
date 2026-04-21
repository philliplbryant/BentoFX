package software.coley.bentofx.persistence.impl.storage.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class DatabaseLayoutStorageIT {

	private static final String ENTITY_MANAGER_FACTORY_IDENTIFIER = "bentoLayout";
	private static final String TEST_LAYOUT_IDENTIFIER = "test-layout";
	private static final String TEST_CODEC_IDENTIFIER = "none";

	private static final String TEST_DATA =
			"This is test data for the layout.";
	private static final String UPDATED_TEST_DATA =
			"This is updated data for the layout.";

	private static EntityManagerFactory entityManagerFactory;

	private LayoutStorage storage;

	@BeforeAll
	static void setUpAll() {
		entityManagerFactory =
				Persistence.createEntityManagerFactory(
						ENTITY_MANAGER_FACTORY_IDENTIFIER
				);
	}

	@AfterAll
	static void tearDownAll() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
		}
	}

	@BeforeEach
	void setUp() {
		storage =
				new DatabaseLayoutStorage(
						entityManagerFactory,
						TEST_LAYOUT_IDENTIFIER,
						TEST_CODEC_IDENTIFIER
				);

		deleteTestLayout();
	}

	@AfterEach
	void tearDown() {
		deleteTestLayout();
	}

	@Test
	void testDatabaseLayoutStorageInitialNonExistence() {
		assertThat(storage.exists())
				.as("The layout should not exist initially.")
				.isFalse();
	}

	@Test
	void testWriteAndReadData() throws IOException {
		writeData(TEST_DATA);

		assertThat(storage.exists())
				.as("The layout should exist after writing data.")
				.isTrue();

		assertThat(readData())
				.as("Read data should match the written data.")
				.isEqualTo(TEST_DATA);
	}

	@Test
	void testOverwriteData() throws IOException {
		writeData(TEST_DATA);

		assertThat(storage.exists())
				.as("The layout should exist after writing data.")
				.isTrue();

		writeData(UPDATED_TEST_DATA);

		assertThat(readData())
				.as("Read data should match the updated data.")
				.isEqualTo(UPDATED_TEST_DATA);
	}

	private void writeData(final String data) throws IOException {
		try (OutputStream outputStream = storage.openOutputStream()) {
			outputStream.write(data.getBytes(UTF_8));
		}
	}

	private String readData() throws IOException {
		try (InputStream inputStream = storage.openInputStream();
		     BufferedReader reader =
				     new BufferedReader(
						     new InputStreamReader(
								     inputStream,
								     UTF_8
						     )
				     )) {
			final StringBuilder data = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				data.append(line);
			}
			return data.toString();
		}
	}

	private void deleteTestLayout() {
		try (EntityManager entityManager =
				     entityManagerFactory.createEntityManager()) {
			entityManager.getTransaction().begin();

			entityManager.createQuery(
							"DELETE FROM DockingLayoutEntity d " +
									"WHERE d.key.layoutIdentifier = :layoutId " +
									"AND d.key.codecIdentifier = :codecId"
					)
					.setParameter("layoutId", TEST_LAYOUT_IDENTIFIER)
					.setParameter("codecId", TEST_CODEC_IDENTIFIER)
					.executeUpdate();

			entityManager.getTransaction().commit();
		}
	}
}
