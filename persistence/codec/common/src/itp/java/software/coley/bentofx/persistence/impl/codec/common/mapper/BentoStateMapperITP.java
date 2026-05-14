package software.coley.bentofx.persistence.impl.codec.common.mapper;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockableState;
import software.coley.bentofx.persistence.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.state.DragDropStageState;
import software.coley.bentofx.persistence.api.state.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DragDropStageDto;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BentoStateMapperIntegrationTest {
	@Test
	void testFullMappingFromDomainToDtoAndBack() {

		final String expectedBentoIdentifier = "bento-1";
		final String expectedDockableIdentifier = "dockable-1";
		final String expectedLeafIdentifier = "leaf-1";
		final String expectedBranchIdentifier = "branch-1";
		final String expectedRootIdentifier = "root-1";
		final String expectedStageTitle = "Stage Title";

		// Set up the Domain Objects
		DockableState dockableState = new DockableStateBuilder(expectedDockableIdentifier).build();

		DockContainerLeafState leafState = new DockContainerLeafStateBuilder(expectedLeafIdentifier)
				.setPruneWhenEmpty(TRUE)
				.setCanSplit(TRUE)
				.addChildDockableState(dockableState)
				.build();

		DockContainerBranchState branchState = new DockContainerBranchStateBuilder(expectedBranchIdentifier)
				.setPruneWhenEmpty(FALSE)
				.addDockContainerState(leafState)
				.build();

		DockContainerRootBranchState rootState = new DockContainerRootBranchStateBuilder(expectedRootIdentifier)
				.setOrientation(javafx.geometry.Orientation.HORIZONTAL)
				.setPruneWhenEmpty(TRUE)
				.addDockContainerState(branchState)
				.build();

		DragDropStageState stageState = new DragDropStageStateBuilder(TRUE)
				.setTitle(expectedStageTitle)
				.setDockContainerRootBranchState(rootState)
				.build();

		BentoState bentoState = new BentoStateBuilder(expectedBentoIdentifier)
				.addRootBranchState(rootState)
				.addDragDropStageState(stageState)
				.build();

		List<BentoState> bentoStates = new ArrayList<>();
		bentoStates.add(bentoState);

		// Perform Domain to DTO Mapping
		DockingLayoutDto dto = BentoStateMapper.toDto(bentoStates);

		// Validate the DTO
		assertNotNull(dto);
		assertEquals(1, dto.bentoStates.size());
		BentoStateDto bentoStateDto = dto.bentoStates.getFirst();
		assertEquals(expectedBentoIdentifier, bentoStateDto.identifier);
		assertEquals(1, bentoStateDto.rootBranches.size());
		assertEquals(1, bentoStateDto.dragDropStages.size());

		DockContainerRootBranchDto rootBranchDto = bentoStateDto.rootBranches.getFirst();
		assertEquals(expectedRootIdentifier, rootBranchDto.identifier);
		assertEquals(1, rootBranchDto.branches.size());
		assertEquals(TRUE, rootBranchDto.pruneWhenEmpty);

		DockContainerBranchDto branchDto = rootBranchDto.branches.getFirst();
		assertEquals(expectedBranchIdentifier, branchDto.identifier);
		assertEquals(FALSE, branchDto.pruneWhenEmpty);
		assertEquals(1, branchDto.children.size());

		DockContainerLeafDto leafDto = (DockContainerLeafDto) branchDto.children.getFirst();
		assertEquals(expectedLeafIdentifier, leafDto.identifier);
		assertEquals(TRUE, leafDto.pruneWhenEmpty);
		assertEquals(expectedDockableIdentifier, leafDto.dockables.getFirst().identifier);

		DragDropStageDto stageDto = bentoStateDto.dragDropStages.getFirst();
		assertEquals(TRUE, stageDto.autoCloseWhenEmpty);
		assertEquals(expectedStageTitle, stageDto.title);
		assertNotNull(stageDto.dockContainerRootBranchDto);
		assertEquals(expectedRootIdentifier, stageDto.dockContainerRootBranchDto.identifier);

		// Perform DTO to Domain Mapping
		List<BentoState> deserializedBentoStates = BentoStateMapper.fromDto(dto);

		// Validate the Round-tripped Result
		assertNotNull(deserializedBentoStates);
		assertEquals(1, deserializedBentoStates.size());

		BentoState deserializedBentoState = deserializedBentoStates.getFirst();
		assertEquals(expectedBentoIdentifier, deserializedBentoState.getIdentifier());
		assertEquals(1, deserializedBentoState.getRootBranchStates().size());
		assertEquals(1, deserializedBentoState.getDragDropStageStates().size());

		DockContainerRootBranchState deserializedRoot =
				deserializedBentoState.getRootBranchStates().getFirst();
		assertEquals(expectedRootIdentifier, deserializedRoot.getIdentifier());
		assertEquals(TRUE, deserializedRoot.doPruneWhenEmpty().orElse(FALSE));
		assertEquals(1, deserializedRoot.getChildDockContainerStates().size());

		DockContainerBranchState deserializedBranchState =
				(DockContainerBranchState) deserializedRoot.getChildDockContainerStates().getFirst();
		assertEquals(expectedBranchIdentifier, deserializedBranchState.getIdentifier());
		assertEquals(FALSE, deserializedBranchState.doPruneWhenEmpty().orElse(TRUE));

		DockContainerLeafState deserializedLeafState =
				(DockContainerLeafState) deserializedBranchState.getChildDockContainerStates().getFirst();
		assertEquals(expectedLeafIdentifier, deserializedLeafState.getIdentifier());
		assertEquals(TRUE, deserializedLeafState.doPruneWhenEmpty().orElse(FALSE));
		assertEquals(1, deserializedLeafState.getChildDockableStates().size());

		DockableState deserializedDockableState =
				deserializedLeafState.getChildDockableStates().getFirst();
		assertEquals(expectedDockableIdentifier, deserializedDockableState.getIdentifier());
	}
}
