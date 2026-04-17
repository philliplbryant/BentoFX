package software.coley.bentofx.persistence.impl.storage;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.impl.BentoLayout;
import software.coley.bentofx.persistence.impl.DockingLayout;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockingLayoutBuilderTest {

    @Test
    void dockingLayoutExposesImmutableSnapshotOfBuiltLayouts() throws Exception {
        Constructor<BentoLayout> constructor = BentoLayout.class.getDeclaredConstructor(
                String.class,
                List.class,
                List.class
        );
        constructor.setAccessible(true);

        BentoLayout first = constructor.newInstance("bento-1", List.of(), List.of());
        BentoLayout second = constructor.newInstance("bento-2", List.of(), List.of());

        DockingLayout layout = new DockingLayout.DockingLayoutBuilder()
                .addBentoLayout(first)
                .addBentoLayout(second)
                .build();

        assertThat(layout.getBentoLayouts())
                .extracting(BentoLayout::getIdentifier)
                .containsExactly("bento-1", "bento-2");

        final List<BentoLayout> bentoLayouts = layout.getBentoLayouts();
        assertThatThrownBy(() -> bentoLayouts.add(first))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
