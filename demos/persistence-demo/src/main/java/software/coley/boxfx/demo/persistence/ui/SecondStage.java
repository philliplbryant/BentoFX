package software.coley.boxfx.demo.persistence.ui;

import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.BentoLayout;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.boxfx.demo.persistence.provider.BoxAppBentoProvider;

import java.util.List;


/**
 * Just a plain ole {@code Stage} without any docking controls.
 */
public class SecondStage extends Stage {

    private static final Logger logger =
            LoggerFactory.getLogger(SecondStage.class);
    private final @NotNull StageIconImageProvider stageIconImageProvider;
    private final @NotNull Bento bento = new Bento("second-stage-bento");

    public SecondStage(
            final @NotNull BoxAppBentoProvider bentoProvider,
            final @NotNull StageIconImageProvider stageIconImageProvider
    ) {
        bentoProvider.addBento(bento);
        this.stageIconImageProvider = stageIconImageProvider;
        init();
    }

    public @NotNull Bento getBento() {
        return bento;
    }

    public void restoreLayout(final @NotNull BentoLayout bentoLayout) {

        @NotNull List<DockContainerRootBranch> rootBranches =
                bentoLayout.getRootBranches();

        // This stage does not have any root branches
        if(!rootBranches.isEmpty()) {
            logger.error(
                    "The SecondStage should not have any root branches but {}" +
                            " were found.",
                    rootBranches.size()
            );
        }

        show();

        // Restore any new drag drop stages that were created apart from the
        // default layout.
        for (final @NotNull DragDropStage dragDropStage :
                bentoLayout.getDragDropStages()) {
            dragDropStage.show();
        }
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
