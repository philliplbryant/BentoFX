package software.coley.bentofx.persistence.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines the BentoFX docking layout for persistence.
 *
 * @author Phil Bryant
 */
public class DockingLayout {

    private final List<BentoLayout> bentoLayouts;

    private DockingLayout(
            final List<BentoLayout> bentoLayouts
    ) {
            this.bentoLayouts = bentoLayouts;
    }

    public List<BentoLayout> getBentoLayouts() {
        return List.copyOf(bentoLayouts);
    }


    public static class DockingLayoutBuilder {

        private final List<BentoLayout> bentoLayouts =
                new ArrayList<>();

        public DockingLayoutBuilder addBentoLayout(
                final BentoLayout bentoLayout
        ) {
            bentoLayouts.add(Objects.requireNonNull(bentoLayout));
            return this;
        }

        public DockingLayout build() {
            return new DockingLayout(bentoLayouts);
        }
    }
}
