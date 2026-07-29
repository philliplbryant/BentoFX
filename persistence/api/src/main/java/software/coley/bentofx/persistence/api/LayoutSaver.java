package software.coley.bentofx.persistence.api;

/**
 * The Application Programming Interface for outputting a BentoFX layout for
 * persistence.
 *
 * @author Phil Bryant
 */
public interface LayoutSaver extends AutoCloseable {

    void saveLayout() throws BentoStateException;

    @Override
    default void close() {
        // Default no-op. Implementations that own resources should override.
    }
}
