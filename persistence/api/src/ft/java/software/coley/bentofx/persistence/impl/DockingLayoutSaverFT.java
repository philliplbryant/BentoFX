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
import software.coley.bentofx.persistence.testfixtures.storage.InMemoryLayoutStorage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class DockingLayoutSaverFT {

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
            stage.setTitle("Detached");
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

        assertThat(storage.exists()).isTrue();
        assertThat(storage.toByteArray()).isNotEmpty();
        assertThat(bentoStates).hasSize(1);

        BentoState saved = bentoStates.getFirst();
        assertThat(saved.getRootBranchStates())
                .extracting(IdentifiableState::getIdentifier)
                .containsExactly(mainRootBranchId);
        assertThat(saved.getDragDropStageStates()).hasSize(1);
        assertThat(saved.getDragDropStageStates().getFirst().getTitle()).contains("Detached");
        assertThat(saved.getDragDropStageStates().getFirst()
                .getDockContainerRootBranchState()
                .map(IdentifiableState::getIdentifier))
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

        assertThat(codec.getEncodedStates()).isEmpty();
        assertThat(storage.toByteArray()).isNotEmpty();
    }
}
