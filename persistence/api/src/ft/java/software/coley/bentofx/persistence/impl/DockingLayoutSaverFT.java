package software.coley.bentofx.persistence.impl;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.building.DockBuilding;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.layout.container.DockContainerLeaf;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.IdentifiableState;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.bentofx.persistence.testfixtures.codec.InMemoryLayoutCodec;
import software.coley.bentofx.persistence.testfixtures.codec.ThreadRecordingLayoutCodec;
import software.coley.bentofx.persistence.testfixtures.storage.InMemoryLayoutStorage;
import software.coley.bentofx.persistence.testfixtures.storage.ThreadRecordingLayoutStorage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class DockingLayoutSaverFT {

    private static final String DETACHED_STAGE_TITLE = "Detached";

    private static final String CODEC_GET_ENCODETHREAD_DESCRIPTION = "codec.getEncodeThread()";
    private static final String STORAGE_GET_OPENOUTPUTSTREAMTHREAD_DESCRIPTION = "storage.getOpenOutputStreamThread()";
    private static final String STORAGE_TOBYTEARRAY_DESCRIPTION = "storage.toByteArray()";

    @Test
    void saveLayoutEncodesNonStageRootsAndDragDropStagesWithoutDuplicates(FxRobot robot) throws BentoStateException {
        Bento bento = new Bento();
        DockBuilding dockBuilding = bento.dockBuilding();

        // Main
        final String mainRootBranchId = "root-main";
        final String mainLeafId = "leaf-main";
        final String mainDockableId = "dock-main";
        final String mainDockableTitle = "Main Dock";

        // DragDropStage
        final String dragDropRootBranchId = "root-drag";

        DockContainerRootBranch mainRoot = dockBuilding.root(mainRootBranchId);
        DockContainerLeaf mainLeaf = dockBuilding.leaf(mainLeafId);
        Dockable mainDockable = dockBuilding.dockable(mainDockableId);
        mainDockable.setTitle(mainDockableTitle);
        mainLeaf.addDockable(mainDockable);
        mainRoot.addContainer(mainLeaf);

        DockContainerRootBranch dragRoot = dockBuilding.root(dragDropRootBranchId);
        DockContainerLeaf dragLeaf = dockBuilding.leaf("leaf-drag");
        Dockable dragDockable = dockBuilding.dockable("dock-drag");
        dragDockable.setTitle("Drag Dock");
        dragLeaf.addDockable(dragDockable);
        dragRoot.addContainer(dragLeaf);

        AtomicReference<Stage> mainStageRef = new AtomicReference<>();
        AtomicReference<DragDropStage> stageRef = new AtomicReference<>();
        robot.interact(() -> {
            Stage mainStage = new Stage();
            mainStage.setScene(new Scene((Parent) mainRoot));
            mainStage.show();
            mainStageRef.set(mainStage);

            DragDropStage stage = new DragDropStage(true);
            stage.setTitle(DETACHED_STAGE_TITLE);
            stage.setX(200);
            stage.setY(150);
            stage.setWidth(600);
            stage.setHeight(400);
            stage.setScene(new Scene((Parent) dragRoot));
            stage.show();
            stageRef.set(stage);
        });

        DefaultBentoProvider bentoProvider = new DefaultBentoProvider();
        bentoProvider.addBento(bento);
        InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        InMemoryLayoutStorage storage = new InMemoryLayoutStorage();

        try (DockingLayoutSaver saver = new DockingLayoutSaver(
                codec, storage, bentoProvider
        )) {

            saver.saveLayout();
        }

        final List<BentoState> bentoStates = codec.getEncodedStates();

        assertThat(storage.exists())
                .describedAs("storage.exists()")
                .isTrue();
        assertThat(storage.toByteArray())
                .describedAs(STORAGE_TOBYTEARRAY_DESCRIPTION)
                .isNotEmpty();
        assertThat(bentoStates)
                .describedAs("bentoStates")
                .hasSize(1);

        BentoState saved = bentoStates.getFirst();
        assertThat(saved.getRootBranchStates())
                .describedAs("saved.getRootBranchStates()")
                .extracting(IdentifiableState::getIdentifier)
                .containsExactly(mainRootBranchId);
        assertThat(saved.getDragDropStageStates())
                .describedAs("saved.getDragDropStageStates()")
                .hasSize(1);
        assertThat(saved.getDragDropStageStates().getFirst().getTitle())
                .describedAs("saved.getDragDropStageStates().getFirst().getTitle()")
                .contains(DETACHED_STAGE_TITLE);
        assertThat(saved.getDragDropStageStates().getFirst()
                .getDockContainerRootBranchState()
                .map(IdentifiableState::getIdentifier))
                .describedAs("saved.getDragDropStageStates().getFirst() .getDockContainerRootBran...")
                .contains(dragDropRootBranchId);

        robot.interact(() -> {
            stageRef.get().hide();
            mainStageRef.get().hide();
        });
    }

    @Test
    void saveLayoutStillWritesWhenNoBentosExist() throws BentoStateException {
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage();
        final BentoProvider emptyBentoProvider = new DefaultBentoProvider();

        try (DockingLayoutSaver saver = new DockingLayoutSaver(
                codec, storage, emptyBentoProvider

        )) {

            saver.saveLayout();
        }

        assertThat(codec.getEncodedStates())
                .describedAs("codec.getEncodedStates()")
                .isEmpty();
        assertThat(storage.toByteArray())
                .describedAs(STORAGE_TOBYTEARRAY_DESCRIPTION)
                .isNotEmpty();
    }
    @Test
    void saveLayoutEncodesAndWritesAwayFromFxThreadWhenCalledOnFxThread(FxRobot robot) {
        final Bento bento = new Bento();
        final DockBuilding dockBuilding = bento.dockBuilding();
        final DockContainerRootBranch root = dockBuilding.root("root");
        root.addContainer(dockBuilding.leaf("leaf"));

        final ThreadRecordingLayoutCodec codec = new ThreadRecordingLayoutCodec();
        final ThreadRecordingLayoutStorage storage = new ThreadRecordingLayoutStorage();
        final BentoProvider bentoProvider = new DefaultBentoProvider(bento);

        final AtomicReference<Thread> fxThread = new AtomicReference<>();

        robot.interact(() -> {
            fxThread.set(Thread.currentThread());
            try (DockingLayoutSaver saver = new DockingLayoutSaver(
                    codec, storage, bentoProvider
            )) {
                saver.saveLayout();
            } catch (final BentoStateException e) {
                throw new AssertionError(e);
            }
        });

        assertThat(fxThread.get())
                .describedAs("fxThread.get()")
                .isNotNull();
        assertThat(codec.getEncodeThread())
                .describedAs(CODEC_GET_ENCODETHREAD_DESCRIPTION)
                .isNotNull();
        assertThat(storage.getOpenOutputStreamThread())
                .describedAs(STORAGE_GET_OPENOUTPUTSTREAMTHREAD_DESCRIPTION)
                .isNotNull();
        assertThat(codec.getEncodeThread())
                .describedAs(CODEC_GET_ENCODETHREAD_DESCRIPTION)
                .isNotEqualTo(fxThread.get());
        assertThat(storage.getOpenOutputStreamThread())
                .describedAs(STORAGE_GET_OPENOUTPUTSTREAMTHREAD_DESCRIPTION)
                .isNotEqualTo(fxThread.get());
    }
}
