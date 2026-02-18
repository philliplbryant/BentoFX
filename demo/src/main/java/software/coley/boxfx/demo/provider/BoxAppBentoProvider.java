package software.coley.boxfx.demo.provider;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.event.DockEvent;
import software.coley.bentofx.persistence.api.provider.BentoProvider;

import java.util.Optional;

public class BoxAppBentoProvider implements BentoProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(BoxAppBentoProvider.class);

    public static final String IDENTIFIER = "box-app";

    private static final class BoxAppBento extends Bento {

        private BoxAppBento() {

            placeholderBuilding().setDockablePlaceholderFactory(dockable ->
                    new Label("Empty Dockable")
            );

            placeholderBuilding().setContainerPlaceholderFactory(container ->
                    new Label("Empty Container")
            );

            events().addEventListener((DockEvent event) -> {
                if (event instanceof DockEvent.DockableClosing closingEvent)
                    handleDockableClosing(closingEvent);
            });
        }

        @Override
        public @NotNull String getIdentifier() {
            return IDENTIFIER;
        }

        private void handleDockableClosing(@NotNull DockEvent.DockableClosing closingEvent) {
            final Dockable dockable = closingEvent.dockable();
            if (!dockable.getTitle().startsWith("Class "))
                return;

            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Save changes to [" + dockable.getTitle() + "] before closing?");
            alert.getButtonTypes().setAll(
                    ButtonType.YES,
                    ButtonType.NO,
                    ButtonType.CANCEL
            );

            final ButtonType result = alert.showAndWait()
                    .orElse(ButtonType.CANCEL);

            if (result.equals(ButtonType.YES)) {
                // simulate saving application (not docking layout) state
                logger.debug("Saving {}...", dockable.getTitle());

            } else if (result.equals(ButtonType.NO)) {

                // nothing to do - just close
                logger.debug("Closing {} without saving...", dockable.getTitle());

            } else if (result.equals(ButtonType.CANCEL)) {

                // prevent closing
                closingEvent.cancel();
            }
        }
    }

    private static final Bento BOX_APP_BENTO = new BoxAppBento();

    @Override
    public @NotNull Optional<@NotNull Bento> getBento(String identifier) {
        return Optional.of(BOX_APP_BENTO);
    }
}
