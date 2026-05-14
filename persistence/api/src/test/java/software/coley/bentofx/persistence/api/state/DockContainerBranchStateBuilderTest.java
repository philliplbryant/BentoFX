package software.coley.bentofx.persistence.api.state;

import javafx.geometry.Orientation;
import javafx.geometry.Side;
import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;

import java.util.List;
import java.util.Map;

import static javafx.geometry.Orientation.VERTICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockContainerBranchStateBuilderTest {

    @Test
    void branchBuilderCapturesOrientationDividerPositionsAndNestedChildren() {

        final String expectedBranchName = "branch-1";
        final boolean expectedPruneWhenEmpty = false;
        final Orientation expectedOrientation = VERTICAL;
        final double expectedDividerPosition0 = 0.25;
        final double expectedDividerPosition1 = 0.75;

        DockContainerLeafState childLeaf =
                new DockContainerLeafStateBuilder("leaf-child")
                        .setSide(Side.BOTTOM)
                        .build();

        DockContainerBranchState branchState =
                new DockContainerBranchStateBuilder(expectedBranchName)
                        .setPruneWhenEmpty(expectedPruneWhenEmpty)
                        .setOrientation(expectedOrientation)
                        .addDividerPosition(0, expectedDividerPosition0)
                        .addDividerPosition(1, expectedDividerPosition1)
                        .addDockContainerState(childLeaf)
                        .build();

        assertThat(branchState.getIdentifier())
                .isEqualTo(expectedBranchName);

        assertThat(branchState.doPruneWhenEmpty())
                .contains(expectedPruneWhenEmpty);

        assertThat(branchState.getOrientation())
                .contains(expectedOrientation);

        assertThat(branchState.getDividerPositions())
                .containsEntry(0, expectedDividerPosition0)
                .containsEntry(1, expectedDividerPosition1);

        assertThat(branchState.getChildDockContainerStates())
                .containsExactly(childLeaf);

        final Map<Integer, Double> dividerPositions =
                branchState.getDividerPositions();
        assertThatThrownBy(() -> dividerPositions.put(2, 0.5))
                .isInstanceOf(UnsupportedOperationException.class);

        final List<DockContainerState> dockContainerStates =
                branchState.getChildDockContainerStates();
        assertThatThrownBy(() -> dockContainerStates.add(childLeaf))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
