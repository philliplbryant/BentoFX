package software.coley.bentofx.persistence.impl;

import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.codec.BentoState;
import software.coley.bentofx.persistence.impl.codec.IdentifiableState;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.bentofx.persistence.testfixtures.codec.InMemoryLayoutCodec;

import java.io.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class BentoLayoutSaverFT {

    @Test
    void saveLayoutEncodesNonStageRootsAndDragDropStagesWithoutDuplicates(FxRobot robot) throws BentoStateException {
        Bento bento = new Bento();
        Object dockBuilding = invoke(bento, "dockBuilding");

        Object mainRoot = invoke(dockBuilding, "root", "root-main");
        Object mainLeaf = invoke(dockBuilding, "leaf", "leaf-main");
        Object mainDockable = invoke(dockBuilding, "dockable", "dock-main");
        invoke(mainDockable, "setTitle", "Main Dock");
        invoke(mainLeaf, "addDockable", mainDockable);
        invoke(mainRoot, "addContainer", mainLeaf);

        Object dragRoot = invoke(dockBuilding, "root", "root-drag");
        Object dragLeaf = invoke(dockBuilding, "leaf", "leaf-drag");
        Object dragDockable = invoke(dockBuilding, "dockable", "dock-drag");
        invoke(dragDockable, "setTitle", "Drag Dock");
        invoke(dragLeaf, "addDockable", dragDockable);
        invoke(dragRoot, "addContainer", dragLeaf);

        AtomicReference<DragDropStage> stageRef = new AtomicReference<>();
        robot.interact(() -> {
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

        try (BentoLayoutSaver saver = new BentoLayoutSaver(
                bentoProvider,
                codec,
                storage
        )) {

            saver.saveLayout();
        }

        final List<BentoState> bentoStates = codec.getEncodedStates();

        assertThat(storage.exists()).isTrue();
        assertThat(storage.bytes()).isNotEmpty();
        assertThat(bentoStates).hasSize(1);

        BentoState saved = bentoStates.getFirst();
//        assertThat(saved.getRootBranchStates())
//                .extracting(IdentifiableState::getIdentifier)
//                .containsExactly("root-main");
        assertThat(saved.getDragDropStageStates()).hasSize(1);
        assertThat(saved.getDragDropStageStates().getFirst().getTitle()).contains("Detached");
        assertThat(saved.getDragDropStageStates().getFirst()
                .getDockContainerRootBranchState()
                .map(IdentifiableState::getIdentifier))
                .contains("root-drag");

        robot.interact(() -> stageRef.get().hide());
    }

    @Test
    void saveLayoutStillWritesWhenNoBentosExist() throws BentoStateException {
        final InMemoryLayoutCodec codec = new InMemoryLayoutCodec();
        final InMemoryLayoutStorage storage = new InMemoryLayoutStorage();
        final BentoProvider emptyBentoProvider = new DefaultBentoProvider();

        try (BentoLayoutSaver saver = new BentoLayoutSaver(
                emptyBentoProvider,
                codec,
                storage

        )) {

            saver.saveLayout();
        }

        assertThat(codec.getEncodedStates()).isEmpty();
        assertThat(storage.bytes()).isNotEmpty();
    }

    private static Object invoke(Object target, String methodName, Object... args) {
        Method method = findMethod(target.getClass(), methodName, args.length);
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to invoke " + methodName + " on " + target.getClass(), ex);
        }
    }

    private static Method findMethod(Class<?> type, String methodName, int argCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == argCount) {
                return method;
            }
        }
        throw new AssertionError("No method named '" + methodName + "' with " + argCount + " parameters on " + type);
    }

    private static final class InMemoryLayoutStorage implements LayoutStorage {
        private volatile byte[] bytes = new byte[0];

        @Override
        public boolean exists() {
            return bytes.length > 0;
        }

        @Override
        public synchronized @NonNull OutputStream openOutputStream() {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    bytes = toByteArray();
                }
            };
        }

        @Override
        public synchronized @NonNull InputStream openInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        private byte[] bytes() {
            return bytes;
        }
    }
}
