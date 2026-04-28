package software.coley.bentofx.persistence.impl;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import software.coley.bentofx.Bento;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.api.provider.BentoProvider;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.storage.LayoutStorage;
import software.coley.bentofx.persistence.impl.DockingLayout.DockingLayoutBuilder;
import software.coley.bentofx.persistence.impl.codec.BentoState;
import software.coley.bentofx.persistence.impl.codec.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DockableState;
import software.coley.bentofx.persistence.impl.codec.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.impl.codec.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;
import software.coley.bentofx.persistence.testfixtures.codec.InMemoryLayoutCodec;
import software.coley.bentofx.persistence.testfixtures.storage.InMemoryLayoutStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class BentoLayoutRestorerFT {

    @Test
    void restoreLayoutReturnsDefaultWhenStorageDoesNotExist() throws Exception {
        LayoutStorage storage = new InMemoryLayoutStorage(false);
        LayoutCodec codec = new InMemoryLayoutCodec();
        DockingLayout fallback = new DockingLayoutBuilder().build();
        BentoProvider bentoProvider = new DefaultBentoProvider(new Bento());

        BentoLayoutRestorer restorer = new BentoLayoutRestorer(
                codec,
                storage,
                bentoProvider,
                id -> Optional.empty(),
                null,
                null
        );

        DockingLayout restored = restorer.restoreLayout(() -> fallback);

        assertThat(restored).isSameAs(fallback);
        assertThat(codec.decode(new ByteArrayInputStream(new byte[0]))).isEmpty();
    }

    @Test
    void restoreLayoutBuildsRootBranchesAndDragDropStages(FxRobot robot) throws Exception {
        DockableState dockableState = new DockableStateBuilder("dock-1")
                .setTitle("Dock 1")
                .setTooltip("This is the tooltip text for Dock 1")
                .setDockableNode(new Label("node-1"))
                .setClosable(true)
                .build();

        DockContainerLeafStateBuilder leafBuilder =
                new DockContainerLeafStateBuilder("leaf-1");
        leafBuilder.setSide(Side.LEFT);
        leafBuilder.setCanSplit(true);
        leafBuilder.setResizableWithParent(true);
        leafBuilder.setSelectedDockableStateIdentifier("dock-1");
        leafBuilder.setUncollapsedSizePx(240.0);
        leafBuilder.setCollapsed(false);
        leafBuilder.setPruneWhenEmpty(false);
        leafBuilder.addChildDockableState(dockableState);

        DockContainerRootBranchStateBuilder rootBuilder =
                new DockContainerRootBranchStateBuilder("root-1");
        rootBuilder.setOrientation(Orientation.HORIZONTAL);
        rootBuilder.setPruneWhenEmpty(false);
        rootBuilder.addDividerPosition(0, 0.35);
        rootBuilder.addDockContainerState(leafBuilder.build());

        DockContainerRootBranchStateBuilder dragRootBuilder =
                new DockContainerRootBranchStateBuilder("root-drag");
        dragRootBuilder.setOrientation(Orientation.VERTICAL);
        dragRootBuilder.setPruneWhenEmpty(true);

        BentoState state = new BentoState.BentoStateBuilder("bento-1")
                .addRootBranchState(rootBuilder.build())
                .addDragDropStageState(new DragDropStageStateBuilder(true)
                        .setTitle("drag-stage")
                        .setX(120.0)
                        .setY(75.0)
                        .setWidth(640.0)
                        .setHeight(480.0)
                        .setOpacity(0.8)
                        .setResizable(true)
                        .setAlwaysOnTop(false)
                        .setModality(Modality.NONE)
                        .setDockContainerRootBranchState(dragRootBuilder.build())
                        .build())
                .build();

        LayoutCodec codec = new InMemoryLayoutCodec();
        codec.encode(
                List.of(state),
                new ByteArrayOutputStream(0)
        );

        StageIconImageProvider stageIconImageProvider =
                () -> List.of(
                        new WritableImage(1, 1)
                );

        BentoLayoutRestorer restorer = new BentoLayoutRestorer(
                codec,
                new InMemoryLayoutStorage(true),
                new DefaultBentoProvider(),
                id ->
                        "dock-1".equals(id)
                                ? Optional.of(dockableState)
                                : Optional.empty(),
                stageIconImageProvider,
                null
        );

        AtomicReference<DockingLayout> restoredRef = new AtomicReference<>();
        robot.interact(() ->
                restoredRef.set(
                        restorer.restoreLayout(
                                () ->
                                        new DockingLayoutBuilder().build()
                        )
                )
        );

        DockingLayout restored = restoredRef.get();
        assertThat(restored.getBentoLayouts())
                .hasSize(1);

        BentoLayout bentoLayout = restored.getBentoLayouts().getFirst();
        assertThat(bentoLayout.getIdentifier())
                .isEqualTo("bento-1");

        assertThat(bentoLayout.getRootBranches())
                .hasSize(1);
        assertThat(bentoLayout.getDragDropStages())
                .hasSize(1);

        Object root = bentoLayout.getRootBranches().getFirst();
        assertThat(invoke(root, "getIdentifier"))
                .isEqualTo("root-1");
        assertThat(invoke(root, "getOrientation"))
                .isEqualTo(Orientation.HORIZONTAL);

        @SuppressWarnings("unchecked")
        List<Object> rootContainers =
                (List<Object>) invoke(root, "getChildContainers");
        assertThat(rootContainers)
                .hasSize(1);

        Object leaf = rootContainers.getFirst();
        assertThat(invoke(leaf, "getIdentifier"))
                .isEqualTo("leaf-1");
        assertThat(invoke(leaf, "getSide"))
                .isEqualTo(Side.LEFT);

        @SuppressWarnings("unchecked")
        List<Object> dockables =
                (List<Object>) invoke(leaf, "getDockables");
        assertThat(dockables)
                .hasSize(1);
        assertThat(invoke(dockables.getFirst(), "getIdentifier"))
                .isEqualTo("dock-1");
        assertThat(invoke(dockables.getFirst(), "getTitle"))
                .isEqualTo("Dock 1");

        Stage dragStage = bentoLayout.getDragDropStages().getFirst();
        assertThat(dragStage.getTitle())
                .isEqualTo("drag-stage");
        assertThat(dragStage.getX())
                .isEqualTo(120.0);
        assertThat(dragStage.getY())
                .isEqualTo(75.0);
        assertThat(dragStage.getWidth())
                .isEqualTo(640.0);
        assertThat(dragStage.getHeight())
                .isEqualTo(480.0);
        assertThat(dragStage.getOpacity())
                .isEqualTo(0.8);
        assertThat(dragStage.isResizable())
                .isTrue();
        assertThat(dragStage.getIcons())
                .hasSize(1);
        assertThat(invoke(
                dragStage.getScene().getRoot(),
                "getIdentifier")
        )
                .isEqualTo("root-drag");

        robot.interact(dragStage::hide);
    }

    private static Object invoke(
            Object target,
            String methodName,
            Object... args
    ) {
        Method method = findMethod(
                target.getClass(),
                methodName,
                args.length
        );
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(
                    "Failed to invoke " + methodName + " on " +
                            target.getClass(),
                    ex
            );
        }
    }

    private static Method findMethod(
            Class<?> type,
            String methodName,
            int argCount
    ) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) &&
                    method.getParameterCount() == argCount) {
                return method;
            }
        }
        throw new AssertionError(
                "No method named '" + methodName + "' with " +
                        argCount + " parameters on " + type
        );
    }
}
