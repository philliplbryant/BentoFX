package software.coley.boxfx.demo.ui;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.Bento;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.IdentifiableStageLayout;
import software.coley.bentofx.persistence.api.PersistableStage;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.boxfx.demo.provider.BoxAppBentoProvider;

import java.util.ArrayList;
import java.util.List;

import static software.coley.bentofx.persistence.impl.StageUtils.getStageStateBuilder;

/**
 * Although persistable, this is really just a plain ole {@code Stage} without
 * any docking controls. However, because it's also a {@link PersistableStage},
 * its layout will be saved and restored.
 */
public class SecondStage extends PersistableStage {

    private final @NotNull StageIconImageProvider stageIconImageProvider;
    private final @NotNull Bento bento = new Bento("second-stage-bento");
    private final @NotNull List<DockContainerRootBranch> rootBranches =
            new ArrayList<>();

    public SecondStage(
            final @NotNull BoxAppBentoProvider bentoProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider
    ) {
        super("second-stage");
        bentoProvider.addBento(bento);
        this.stageIconImageProvider = stageIconImageProvider;
        init();
    }

    @Override
    public @NotNull Bento getBento() {
        return bento;
    }

    @Override
    public @NotNull IdentifiableStageLayout getLayout() {
        return new IdentifiableStageLayout(
                getIdentifier(),
                getStageStateBuilder(this).build(),
                rootBranches
        );
    }

    private void init() {
        setTitle("Second Stage");
        getIcons().addAll(
                stageIconImageProvider.getStageIcons()
        );
        setWidth(300);
        setHeight(175);
        setX(-300);
        setY(0);
    }
}
