package software.coley.boxfx.demo;

import javafx.application.Application;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Starts the {@link BoxApp} application.
 *
 * @author Matt Coley
 * @author Phil Bryant
 */
public class Runner {

    private static final String LOGGING_PROPERTIES = "logging.properties";

    // Using standard outputs when errors occur during logging initializing.
    @SuppressWarnings("java:S106")
    public static void main(String[] args) {

        // Initialize java.util.logging
        try (InputStream inputStream = BoxApp.class.getResourceAsStream(
                "/" + LOGGING_PROPERTIES
        )) {
            if (inputStream != null) {
                LogManager.getLogManager().readConfiguration(inputStream);
            } else {
                System.err.println(
                        "Could not read " + LOGGING_PROPERTIES + ". Using " +
                                "default Java Utility Logging configuration."
                );
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        // Launch the application
        Application.launch(BoxApp.class, args);
    }
}
