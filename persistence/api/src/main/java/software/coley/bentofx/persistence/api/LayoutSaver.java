package software.coley.bentofx.persistence.api;

/**
 * The Application Programming Interface for outputting a BentoFX layout for
 * persistence.
 *
 * @author Phil Bryant
 */
public interface LayoutSaver extends AutoCloseable {

    /**
     * Saves the current docking layout.
     *
     * <p>Capturing the layout requires the JavaFX application thread, so this
     * must be called while the JavaFX toolkit is still running. Call it before
     * {@code Platform.exit()}, not from a shutdown hook that runs afterwards:
     * once the toolkit has stopped, work handed to it is accepted and never
     * run.</p>
     *
     * @throws BentoStateException when the layout cannot be saved.
     */
    void saveLayout() throws BentoStateException;

    /**
     * Releases resources held by this saver, saving the layout first when the
     * implementation supports it.
     *
     * <p>Close the saver <em>before</em> shutting the JavaFX toolkit down. An
     * implementation that saves here needs the JavaFX application thread to
     * capture the layout, and after {@code Platform.exit()} that thread will
     * never run the request. Implementations bound their wait, so a misordered
     * shutdown costs the final save rather than hanging, but the save is still
     * lost.</p>
     */
    @Override
    default void close() {
        // Default no-op. Implementations that own resources should override.
    }
}
