package software.coley.boxfx.demo.persistence.ui;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.Bento;
import software.coley.bentofx.control.DragDropStage;
import software.coley.bentofx.layout.container.DockContainerRootBranch;
import software.coley.bentofx.persistence.api.provider.StageIconImageProvider;
import software.coley.bentofx.persistence.api.BentoLayout;
import software.coley.bentofx.persistence.impl.provider.DefaultBentoProvider;

import java.util.List;


/**
 * Just a plain ole {@code Stage} without any docking controls and thus nothing
 * to be persisted.
 *
 * @author Phil Bryant
 */
public class SecondStage extends Stage {

    private static final Logger logger =
            LoggerFactory.getLogger(SecondStage.class);
    private final StageIconImageProvider stageIconImageProvider;
    private final Bento bento = new Bento("second-stage-bento");

    public SecondStage(
            final DefaultBentoProvider bentoProvider,
            final StageIconImageProvider stageIconImageProvider
    ) {
        bento.stageBuilding().setApplyMousePosition(true);
        bento.stageBuilding().setApplySourceAsOwner(false);
        bentoProvider.addBento(bento);
        this.stageIconImageProvider = stageIconImageProvider;
        init();
    }

    public Bento getBento() {
        return bento;
    }

    public void restoreLayout(final BentoLayout bentoLayout) {

        List<DockContainerRootBranch> rootBranches =
                bentoLayout.getRootBranches();

        // This stage does not have any root branches
        if (!rootBranches.isEmpty()) {
            logger.error(
                    "The SecondStage should not have any root branches but {}" +
                            " were found.",
                    rootBranches.size()
            );
        }

        show();

        // Restore any new drag drop stages that were created apart from the
        // default layout.
        for (final DragDropStage dragDropStage :
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
        setX(1450);
        setY(110);
    }
}
