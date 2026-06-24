package software.coley.bentofx.persistence.impl.codec.common.mapper;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.*;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.state.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BentoStateMapperIntegrationTest {
	// This test follows the "Single Act Rule" and only exercises the unit
	// under test (BentoStateMapper) once each: for mapping to and from a DTO.
	// As such, we are suppressing the warning for the number of assertions
	// ("Single Assert Rule").
	@SuppressWarnings("java:S5961")
	@Test
	void testFullMappingFromDomainToDtoAndBack() throws BentoStateException {

		final String expectedBentoIdentifier = "bento-1";
		final String expectedDockableIdentifier = "dockable-1";
		final String expectedLeafIdentifier = "leaf-1";
		final String expectedBranchIdentifier = "branch-1";
		final String expectedRootIdentifier = "root-1";
		final String expectedStageTitle = "Stage Title";

		// Set up the Domain Objects
		final DockableState dockableState = new DockableStateBuilder(expectedDockableIdentifier).build();

		final DockContainerLeafState leafState = new DockContainerLeafStateBuilder(expectedLeafIdentifier)
				.setPruneWhenEmpty(TRUE)
				.setCanSplit(TRUE)
				.addChildDockableState(dockableState)
				.build();

		final DockContainerBranchState branchState = new DockContainerBranchStateBuilder(expectedBranchIdentifier)
				.setPruneWhenEmpty(FALSE)
				.addDockContainerState(leafState)
				.build();

		final DockContainerRootBranchState rootState = new DockContainerRootBranchStateBuilder(expectedRootIdentifier)
				.setOrientation(javafx.geometry.Orientation.HORIZONTAL)
				.setPruneWhenEmpty(TRUE)
				.addDockContainerState(branchState)
				.build();

		final DragDropStageState stageState = new DragDropStageStateBuilder(TRUE)
				.setTitle(expectedStageTitle)
				.setDockContainerRootBranchState(rootState)
				.build();

		final BentoState bentoState = new BentoStateBuilder(expectedBentoIdentifier)
				.addRootBranchState(rootState)
				.addDragDropStageState(stageState)
				.build();

		final List<BentoState> bentoStates = new ArrayList<>();
		bentoStates.add(bentoState);

		// Perform Domain to DTO Mapping
		final DockingLayoutDto dto = BentoStateMapper.toDto(bentoStates);

		// Validate the DTO
		assertNotNull(dto);

		assertThat(dto.metadata)
				.describedAs("layout metadata")
				.isNotNull();
		assertThat(dto.metadata.schemaVersion)
				.describedAs("schema version")
				.isEqualTo(DockingLayoutDto.getCurrentSchemaVersion());

		assertEquals(1, dto.bentoStates.size());
		BentoStateDto bentoStateDto = dto.bentoStates.getFirst();
		assertEquals(expectedBentoIdentifier, bentoStateDto.identifier);
		assertEquals(1, bentoStateDto.rootBranches.size());
		assertEquals(1, bentoStateDto.dragDropStages.size());

		final DockContainerRootBranchDto rootBranchDto = bentoStateDto.rootBranches.getFirst();
		assertEquals(expectedRootIdentifier, rootBranchDto.identifier);
		assertEquals(1, rootBranchDto.branches.size());
		assertEquals(TRUE, rootBranchDto.pruneWhenEmpty);

		final DockContainerBranchDto branchDto = rootBranchDto.branches.getFirst();
		assertEquals(expectedBranchIdentifier, branchDto.identifier);
		assertEquals(FALSE, branchDto.pruneWhenEmpty);
		assertEquals(1, branchDto.children.size());

		final DockContainerLeafDto leafDto = (DockContainerLeafDto) branchDto.children.getFirst();
		assertEquals(expectedLeafIdentifier, leafDto.identifier);
		assertEquals(TRUE, leafDto.pruneWhenEmpty);
		assertEquals(expectedDockableIdentifier, leafDto.dockables.getFirst().identifier);

		final DragDropStageDto stageDto = bentoStateDto.dragDropStages.getFirst();
		assertEquals(TRUE, stageDto.autoCloseWhenEmpty);
		assertEquals(expectedStageTitle, stageDto.title);
		assertNotNull(stageDto.dockContainerRootBranchDto);
		assertEquals(expectedRootIdentifier, stageDto.dockContainerRootBranchDto.identifier);

		// Perform DTO to Domain Mapping
		final List<BentoState> deserializedBentoStates = BentoStateMapper.fromDto(dto);

		// Validate the Round-tripped Result
		assertNotNull(deserializedBentoStates);
		assertEquals(1, deserializedBentoStates.size());

		final BentoState deserializedBentoState = deserializedBentoStates.getFirst();
		assertEquals(expectedBentoIdentifier, deserializedBentoState.getIdentifier());
		assertEquals(1, deserializedBentoState.getRootBranchStates().size());
		assertEquals(1, deserializedBentoState.getDragDropStageStates().size());

		final DockContainerRootBranchState deserializedRoot =
				deserializedBentoState.getRootBranchStates().getFirst();
		assertEquals(expectedRootIdentifier, deserializedRoot.getIdentifier());
		assertEquals(TRUE, deserializedRoot.doPruneWhenEmpty().orElse(FALSE));
		assertEquals(1, deserializedRoot.getChildDockContainerStates().size());

		final DockContainerBranchState deserializedBranchState =
				(DockContainerBranchState) deserializedRoot.getChildDockContainerStates().getFirst();
		assertEquals(expectedBranchIdentifier, deserializedBranchState.getIdentifier());
		assertEquals(FALSE, deserializedBranchState.doPruneWhenEmpty().orElse(TRUE));

		final DockContainerLeafState deserializedLeafState =
				(DockContainerLeafState) deserializedBranchState.getChildDockContainerStates().getFirst();
		assertEquals(expectedLeafIdentifier, deserializedLeafState.getIdentifier());
		assertEquals(TRUE, deserializedLeafState.doPruneWhenEmpty().orElse(FALSE));
		assertEquals(1, deserializedLeafState.getChildDockableStates().size());

		final DockableState deserializedDockableState =
				deserializedLeafState.getChildDockableStates().getFirst();
		assertEquals(expectedDockableIdentifier, deserializedDockableState.getIdentifier());
	}
	@Test
	void validateSupportedMetadataAllowsMissingMetadata() {
		assertThatCode(() ->
				BentoStateMapper.validateSupportedMetadata(null)
		)
				.describedAs("missing legacy metadata validation")
				.doesNotThrowAnyException();
	}

	@Test
	void validateSupportedMetadataAllowsMissingSchemaVersion() {
		final LayoutMetadataDto metadata = new LayoutMetadataDto();

		assertThatCode(() ->
				BentoStateMapper.validateSupportedMetadata(metadata)
		)
				.describedAs("missing legacy schema version validation")
				.doesNotThrowAnyException();
	}

	@Test
	void validateSupportedMetadataAllowsCurrentSchemaVersion() {
		final LayoutMetadataDto metadata = new LayoutMetadataDto();
		metadata.schemaVersion = DockingLayoutDto.getCurrentSchemaVersion();

		assertThatCode(() ->
				BentoStateMapper.validateSupportedMetadata(metadata)
		)
				.describedAs("current schema version validation")
				.doesNotThrowAnyException();
	}

	@Test
	void validateSupportedMetadataRejectsInvalidSchemaVersion() {
		final LayoutMetadataDto metadata = new LayoutMetadataDto();
		metadata.schemaVersion = 0;

		assertThatThrownBy(() ->
				BentoStateMapper.validateSupportedMetadata(metadata)
		)
				.describedAs("invalid schema version validation")
				.isInstanceOf(BentoStateException.class)
				.hasMessageContaining("Unsupported BentoFX docking layout schema version: 0");
	}

	@Test
	void validateSupportedMetadataRejectsFutureSchemaVersion() {
		final LayoutMetadataDto metadata = new LayoutMetadataDto();
		metadata.schemaVersion = DockingLayoutDto.getCurrentSchemaVersion() + 1;

		assertThatThrownBy(() ->
				BentoStateMapper.validateSupportedMetadata(metadata)
		)
				.describedAs("future schema version validation")
				.isInstanceOf(BentoStateException.class)
				.hasMessageContaining("Unsupported BentoFX docking layout schema version");
	}

}
