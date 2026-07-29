package software.coley.bentofx.persistence.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockingLayoutBuilderTest {

    private static final String FIRST_BENTO_IDENTIFIER = "bento-1";
    private static final String SECOND_BENTO_IDENTIFIER = "bento-2";

    private static final String LAYOUT_GET_BENTOLAYOUTS_DESCRIPTION = "layout.getBentoLayouts()";

    @Test
    void dockingLayoutExposesImmutableSnapshotOfBuiltLayouts() throws Exception {
        Constructor<BentoLayout> constructor = BentoLayout.class.getDeclaredConstructor(
                String.class,
                List.class,
                List.class
        );
        constructor.setAccessible(true);

        BentoLayout first = constructor.newInstance(FIRST_BENTO_IDENTIFIER, List.of(), List.of());
        BentoLayout second = constructor.newInstance(SECOND_BENTO_IDENTIFIER, List.of(), List.of());

        DockingLayout layout = new DockingLayout.DockingLayoutBuilder()
                .addBentoLayout(first)
                .addBentoLayout(second)
                .build();

        assertThat(layout.getBentoLayouts())
                .describedAs(LAYOUT_GET_BENTOLAYOUTS_DESCRIPTION)
                .extracting(BentoLayout::getIdentifier)
                .containsExactly(FIRST_BENTO_IDENTIFIER, SECOND_BENTO_IDENTIFIER);

        final List<BentoLayout> bentoLayouts = layout.getBentoLayouts();
        assertThatThrownBy(() -> bentoLayouts.add(first))
                .describedAs("exception thrown by () -> bentoLayouts.add(first)")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void builtLayoutIsNotAffectedByLaterBuilderMutation() throws Exception {
        Constructor<BentoLayout> constructor = BentoLayout.class.getDeclaredConstructor(
                String.class,
                List.class,
                List.class
        );
        constructor.setAccessible(true);

        BentoLayout first = constructor.newInstance(FIRST_BENTO_IDENTIFIER, List.of(), List.of());
        BentoLayout second = constructor.newInstance(SECOND_BENTO_IDENTIFIER, List.of(), List.of());

        DockingLayout.DockingLayoutBuilder builder =
                new DockingLayout.DockingLayoutBuilder()
                        .addBentoLayout(first);

        DockingLayout layout = builder.build();

        builder.addBentoLayout(second);

        assertThat(layout.getBentoLayouts())
                .describedAs(LAYOUT_GET_BENTOLAYOUTS_DESCRIPTION)
                .containsExactly(first);
    }

}
