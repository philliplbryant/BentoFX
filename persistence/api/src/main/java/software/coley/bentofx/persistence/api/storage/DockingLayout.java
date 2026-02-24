package software.coley.bentofx.persistence.api.storage;

import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.BentoLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DockingLayout {

    private final @NotNull List<@NotNull BentoLayout> bentoLayouts;

    private DockingLayout(
            final @NotNull List<@NotNull BentoLayout> bentoLayouts
    ) {
            this.bentoLayouts = bentoLayouts;
    }

    public @NotNull List<@NotNull BentoLayout> getBentoLayouts() {
        return List.copyOf(bentoLayouts);
    }


    public static class DockingLayoutBuilder {

        private final @NotNull List<@NotNull BentoLayout> bentoLayouts =
                new ArrayList<>();

        public DockingLayoutBuilder addBentoLayout(
                final @NotNull BentoLayout bentoLayout
        ) {
            bentoLayouts.add(Objects.requireNonNull(bentoLayout));
            return this;
        }

        public @NotNull DockingLayout build() {
            return new DockingLayout(bentoLayouts);
        }
    }
}
