package software.coley.bentofx.persistence.impl.codec;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.persistence.impl.codec.DockableState.DockableStateBuilder;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class DockableStateBuilderFT {

    @Test
    void testDockableBuilder() {

        final String expectedDockableId = "dockable";
        final Node expectedDockableNode = new Label("Dockable node");
        final String expectedTitle = "Selected";
        final int expectedDragGroupMask = 7;
        final boolean expectedClosable = true;
        final Bento expectedBento = new Bento("bento");
        final Dockable expectedDockable =
                new Dockable(expectedBento, expectedDockableId);

        DockableState dockable =
                new DockableStateBuilder(expectedDockableId)
                        .setDockableNode(expectedDockableNode)
                        .setTitle(expectedTitle)
                        .setDockableIconFactory(null)
                        .setDockableMenuFactory(null)
                        .setDragGroupMask(expectedDragGroupMask)
                        .setClosable(expectedClosable)
                        .setDockableConsumer(DockableConsumer::consumeDockable)
                        .build();

        assertThat(dockable.getIdentifier())
                .isEqualTo(expectedDockableId);

        assertThat(dockable.getDockableNode())
                .contains(expectedDockableNode);

        assertThat(dockable.getTitle())
                .contains(expectedTitle);

        assertThat(dockable.getDockableIconFactory())
                .isEmpty();

        assertThat(dockable.getDockableMenuFactory())
                .isEmpty();

        assertThat(dockable.getDragGroupMask())
                .contains(expectedDragGroupMask);

        assertThat(dockable.isClosable())
                .contains(expectedClosable);

        Optional<Consumer<Dockable>> optionalDockableConsumer =
                dockable.getDockableConsumer();
        assertThat(optionalDockableConsumer)
                .isPresent();

        assertThat(DockableConsumer.consumedDockable)
                .isNull();

        optionalDockableConsumer.get().accept(expectedDockable);
        assertThat(DockableConsumer.consumedDockable)
                .isEqualTo(expectedDockable);
    }

    private static class DockableConsumer {

        private static Dockable consumedDockable = null;
        private static void consumeDockable(Dockable dockable) {
            consumedDockable = dockable;
        }
    }
}
