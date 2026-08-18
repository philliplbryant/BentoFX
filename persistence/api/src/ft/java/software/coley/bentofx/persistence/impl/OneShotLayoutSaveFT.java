package software.coley.bentofx.persistence.impl;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.LayoutPersistenceProfile;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.DockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.api.provider.LayoutCodecProvider;
import software.coley.bentofx.persistence.api.provider.LayoutStorageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.bentofx.persistence.impl.provider.DefaultDockingLayoutPersistenceProvider;
import software.coley.bentofx.persistence.testfixtures.codec.InMemoryLayoutCodec;
import software.coley.bentofx.persistence.testfixtures.storage.InMemoryLayoutStorage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for the one-shot save an application uses to keep a layout under a name.
 *
 * <p>It is the same write a {@code LayoutSaver} performs, minus the session: nothing
 * is scheduled, nothing is registered on a {@link Bento}, and the caller is handed no
 * component to close. That the saver behind it is never armed is structural, not
 * observable from out here - what these tests pin is the behavior an application can
 * see: one call encodes and writes exactly once, and a call with nothing attached
 * leaves the stored layout alone rather than replacing it with an empty one.</p>
 *
 * @author Phil Bryant
 */
@ExtendWith(ApplicationExtension.class)
class OneShotLayoutSaveFT {

    private static final String LAYOUT_IDENTIFIER = "one-shot";
    private static final String ENCODE_CALLS_DESCRIPTION = "codec.getEncodeCalls()";

    @Test
    void saveLayoutWritesTheLayoutExactlyOnce(FxRobot robot) throws BentoStateException {
        final Bento bento = new Bento();
        final DockBuilding dockBuilding = bento.dockBuilding();
        final DockContainerRootBranch root = dockBuilding.root("root");
        root.addContainer(dockBuilding.leaf("leaf"));

        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage();

        final DefaultBentoProvider bentoProvider = new DefaultBentoProvider();
        bentoProvider.addBento(bento);

        final DockingLayoutPersistenceProvider persistenceProvider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider(codec)),
                        List.of(storageProvider(storage))
                );

        // A capture only sees root branches that have a Scene, so the layout has to
        // be showing before the save.
        final AtomicReference<Stage> stageRef = new AtomicReference<>();

        robot.interact(() -> {
            final Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            stageRef.set(stage);
        });

        persistenceProvider.saveLayout(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER),
                bentoProvider
        );

        assertThat(storage.exists())
                .describedAs("storage.exists() after a one-shot save")
                .isTrue();
        assertThat(storage.toByteArray())
                .describedAs("storage.toByteArray() after a one-shot save")
                .isNotEmpty();
        assertThat(codec.getEncodeCalls())
                .describedAs(ENCODE_CALLS_DESCRIPTION)
                .hasSize(1);

        robot.interact(() -> stageRef.get().hide());

        assertThat(codec.getEncodeCalls())
                .describedAs(ENCODE_CALLS_DESCRIPTION + " once the layout is taken down")
                .hasSize(1);
    }

    @Test
    void saveLayoutLeavesAStoredLayoutAloneWhenNothingIsAttached() throws BentoStateException {
        final Bento bento = new Bento();
        bento.dockBuilding().root("root-never-attached");

        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final InMemoryLayoutStorage storage =
                new InMemoryLayoutStorage("a-good-layout".getBytes());

        final DefaultBentoProvider bentoProvider = new DefaultBentoProvider();
        bentoProvider.addBento(bento);

        final DockingLayoutPersistenceProvider persistenceProvider =
                new DefaultDockingLayoutPersistenceProvider(
                        List.of(codecProvider(codec)),
                        List.of(storageProvider(storage))
                );

        persistenceProvider.saveLayout(
                LayoutPersistenceProfile.of(LAYOUT_IDENTIFIER),
                bentoProvider
        );

        assertThat(codec.getEncodeCalls())
                .describedAs(ENCODE_CALLS_DESCRIPTION + " with nothing attached")
                .isEmpty();
        assertThat(storage.toByteArray())
                .describedAs("the previously stored layout")
                .isEqualTo("a-good-layout".getBytes());
    }

    /**
     * A provider yielding one specific codec, so that the test can read what was
     * encoded.
     *
     * @param codec the codec to yield.
     */
    private static LayoutCodecProvider codecProvider(final LayoutCodec codec) {
        return new LayoutCodecProvider() {
            @Override
            public String getIdentifier() {
                return "memory";
            }

            @Override
            public boolean isDefault() {
                return true;
            }

            @Override
            public LayoutCodec getLayoutCodec() {
                return codec;
            }
        };
    }

    /**
     * A provider yielding one specific storage, so that the test can read what was
     * written.
     *
     * @param storage the storage to yield.
     */
    private static LayoutStorageProvider storageProvider(
            final LayoutStorage storage
    ) {
        return new LayoutStorageProvider() {
            @Override
            public String getIdentifier() {
                return "memory";
            }

            @Override
            public boolean isDefault() {
                return true;
            }

            @Override
            public LayoutStorage getLayoutStorage(
                    final @NonNull String layoutIdentifier,
                    final @NonNull String codecIdentifier
            ) {
                return storage;
            }
        };
    }
}
