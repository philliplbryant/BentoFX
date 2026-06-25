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

class BentoStateMapperITP {
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
        assertThat(dto)
                .describedAs("dto")
                .isNotNull();

		assertThat(dto.metadata)
				.describedAs("layout metadata")
				.isNotNull();
		assertThat(dto.metadata.schemaVersion)
				.describedAs("schema version")
				.isEqualTo(DockingLayoutDto.getCurrentSchemaVersion());

		assertThat(dto.bentoStates)
		        .describedAs("dto.bentoStates")
		        .hasSize(1);
		BentoStateDto bentoStateDto = dto.bentoStates.getFirst();
		assertThat(bentoStateDto.identifier)
		        .describedAs("bentoStateDto.identifier")
		        .isEqualTo(expectedBentoIdentifier);
		assertThat(bentoStateDto.rootBranches)
		        .describedAs("bentoStateDto.rootBranches")
		        .hasSize(1);
		assertThat(bentoStateDto.dragDropStages)
		        .describedAs("bentoStateDto.dragDropStages")
		        .hasSize(1);

		final DockContainerRootBranchDto rootBranchDto = bentoStateDto.rootBranches.getFirst();
		assertThat(rootBranchDto.identifier)
		        .describedAs("rootBranchDto.identifier")
		        .isEqualTo(expectedRootIdentifier);
		assertThat(rootBranchDto.branches)
		        .describedAs("rootBranchDto.branches")
		        .hasSize(1);
		assertThat(rootBranchDto.pruneWhenEmpty)
		        .describedAs("rootBranchDto.pruneWhenEmpty")
		        .isEqualTo(TRUE);

		final DockContainerBranchDto branchDto = rootBranchDto.branches.getFirst();
		assertThat(branchDto.identifier)
		        .describedAs("branchDto.identifier")
		        .isEqualTo(expectedBranchIdentifier);
		assertThat(branchDto.pruneWhenEmpty)
		        .describedAs("branchDto.pruneWhenEmpty")
		        .isEqualTo(FALSE);
		assertThat(branchDto.children)
		        .describedAs("branchDto.children")
		        .hasSize(1);

		final DockContainerLeafDto leafDto = (DockContainerLeafDto) branchDto.children.getFirst();
		assertThat(leafDto.identifier)
		        .describedAs("leafDto.identifier")
		        .isEqualTo(expectedLeafIdentifier);
		assertThat(leafDto.pruneWhenEmpty)
		        .describedAs("leafDto.pruneWhenEmpty")
		        .isEqualTo(TRUE);
		assertThat(leafDto.dockables.getFirst().identifier)
		        .describedAs("leafDto.dockables.getFirst().identifier")
		        .isEqualTo(expectedDockableIdentifier);

		final DragDropStageDto stageDto = bentoStateDto.dragDropStages.getFirst();
		assertThat(stageDto.autoCloseWhenEmpty)
		        .describedAs("stageDto.autoCloseWhenEmpty")
		        .isEqualTo(TRUE);
		assertThat(stageDto.title)
		        .describedAs("stageDto.title")
		        .isEqualTo(expectedStageTitle);
		assertThat(stageDto.dockContainerRootBranchDto)
		        .describedAs("stageDto.dockContainerRootBranchDto")
		        .isNotNull();
		assertThat(stageDto.dockContainerRootBranchDto.identifier)
		        .describedAs("stageDto.dockContainerRootBranchDto.identifier")
		        .isEqualTo(expectedRootIdentifier);

		// Perform DTO to Domain Mapping
		final List<BentoState> deserializedBentoStates = BentoStateMapper.fromDto(dto);

		// Validate the Round-tripped Result
		assertThat(deserializedBentoStates)
		        .describedAs("deserializedBentoStates")
		        .isNotNull().hasSize(1);

		final BentoState deserializedBentoState = deserializedBentoStates.getFirst();
		assertThat(deserializedBentoState.getIdentifier())
		        .describedAs("deserializedBentoState.getIdentifier()")
		        .isEqualTo(expectedBentoIdentifier);
		assertThat(deserializedBentoState.getRootBranchStates())
		        .describedAs("deserializedBentoState.getRootBranchStates()")
		        .hasSize(1);
		assertThat(deserializedBentoState.getDragDropStageStates())
		        .describedAs("deserializedBentoState.getDragDropStageStates()")
		        .hasSize(1);

		final DockContainerRootBranchState deserializedRoot =
				deserializedBentoState.getRootBranchStates().getFirst();
		assertThat(deserializedRoot.getIdentifier())
		        .describedAs("deserializedRoot.getIdentifier()")
		        .isEqualTo(expectedRootIdentifier);
		assertThat(deserializedRoot.doPruneWhenEmpty())
		        .describedAs("deserializedRoot.doPruneWhenEmpty()")
		        .contains(TRUE);
		assertThat(deserializedRoot.getChildDockContainerStates())
		        .describedAs("deserializedRoot.getChildDockContainerStates()")
		        .hasSize(1);

		final DockContainerBranchState deserializedBranchState =
				(DockContainerBranchState) deserializedRoot.getChildDockContainerStates().getFirst();
		assertThat(deserializedBranchState.getIdentifier())
		        .describedAs("deserializedBranchState.getIdentifier()")
		        .isEqualTo(expectedBranchIdentifier);
		assertThat(deserializedBranchState.doPruneWhenEmpty())
		        .describedAs("deserializedBranchState.doPruneWhenEmpty()")
		        .contains(FALSE);

		final DockContainerLeafState deserializedLeafState =
				(DockContainerLeafState) deserializedBranchState.getChildDockContainerStates().getFirst();
		assertThat(deserializedLeafState.getIdentifier())
		        .describedAs("deserializedLeafState.getIdentifier()")
		        .isEqualTo(expectedLeafIdentifier);
		assertThat(deserializedLeafState.doPruneWhenEmpty())
		        .describedAs("deserializedLeafState.doPruneWhenEmpty()")
		        .contains(TRUE);
		assertThat(deserializedLeafState.getChildDockableStates())
		        .describedAs("deserializedLeafState.getChildDockableStates()")
		        .hasSize(1);

		final DockableState deserializedDockableState =
				deserializedLeafState.getChildDockableStates().getFirst();
		assertThat(deserializedDockableState.getIdentifier())
		        .describedAs("deserializedDockableState.getIdentifier()")
		        .isEqualTo(expectedDockableIdentifier);
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
