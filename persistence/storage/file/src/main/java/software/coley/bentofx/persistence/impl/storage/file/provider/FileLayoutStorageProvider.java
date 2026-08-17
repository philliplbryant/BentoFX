package software.coley.bentofx.persistence.impl.storage.file.provider;

import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutIdentifiers;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.storage.file.FileLayoutStorage;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Implementation of the {@link LayoutStorageProvider} interface for persisting
 * Bento layouts to a file.
 *
 * @author Phil Bryant
 */
public class FileLayoutStorageProvider implements LayoutStorageProvider {

    private static final String IDENTIFIER = "file";

    private static final String DEFAULT_BENTO_DIRECTORY =
            System.getProperty("user.home") + "/.bentofx";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public LayoutStorage getLayoutStorage(
            final String layoutIdentifier,
            final String codecIdentifier
    ) {
        LayoutIdentifiers.requireValid(layoutIdentifier, codecIdentifier);

        final String normalizedFileExtension = codecIdentifier.startsWith(".") ?
                codecIdentifier.substring(1) :
                codecIdentifier;

        return new FileLayoutStorage(
                getLayoutFile(
                        Path.of(DEFAULT_BENTO_DIRECTORY).toAbsolutePath().normalize(),
                        layoutIdentifier + "." + normalizedFileExtension
                )
        );
    }

    /**
     * {@return the file the joined identifiers name inside the layout directory.}
     *
     * <p>Both identifiers come from the application and become one path
     * component, so a separator or a {@code ..} segment in either would put the
     * layout somewhere this provider did not choose.
     * {@link LayoutIdentifiers#requireValid} has already refused those by name;
     * this is the check that does not depend on having listed them, and it still
     * catches what a list cannot - a drive-relative name on Windows, or one the
     * platform rejects outright.</p>
     *
     * @param directory the absolute, normalized directory layouts are kept in.
     * @param fileName the two identifiers joined by a {@code .}.
     * @throws IllegalArgumentException if the identifiers do not name a file
     * directly inside {@code directory}.
     */
    private static File getLayoutFile(
            final Path directory,
            final String fileName
    ) {
        final Path layoutFile;

        try {
            layoutFile = directory.resolve(fileName).normalize();
        } catch (final InvalidPathException e) {
            throw new IllegalArgumentException(
                    "layoutIdentifier and codecIdentifier must name a file, but '"
                            + fileName + "' is not a usable path.",
                    e
            );
        }

        if (!directory.equals(layoutFile.getParent())) {
            throw new IllegalArgumentException(
                    "layoutIdentifier and codecIdentifier must name a file directly in "
                            + directory + ", but '" + fileName + "' resolves to "
                            + layoutFile + "."
            );
        }

        return layoutFile.toFile();
    }
}
