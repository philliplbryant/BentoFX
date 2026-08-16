package software.coley.bentofx.persistence.impl.codec.common.mapper;

import javafx.geometry.Orientation;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.persistence.api.BentoStateException;
import software.coley.bentofx.persistence.api.state.BentoState;
import software.coley.bentofx.persistence.api.state.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState;
import software.coley.bentofx.persistence.api.state.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState;
import software.coley.bentofx.persistence.api.state.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.state.DockContainerState;
import software.coley.bentofx.persistence.api.state.DockableState;
import software.coley.bentofx.persistence.api.state.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.state.DragDropStageState;
import software.coley.bentofx.persistence.api.state.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockableDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockingLayoutDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DragDropStageDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.LayoutMetadataDto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static software.coley.bentofx.persistence.impl.codec.common.mapper.ElementNames.*;

/**
 * Maps between the immutable {@code *State} domain objects and
 * JAXB/Jackson-friendly DTOs.
 *
 * <p>
 * DTOs are intentionally acyclic and preserve child order via {@link List}.
 * </p>
 *
 * @author Phil Bryant
 */
public final class BentoStateMapper {

	private static final Logger logger =
			LoggerFactory.getLogger(BentoStateMapper.class);

	private BentoStateMapper() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Maps a {@code List<BentoState>} to a {@link DockingLayoutDto}.
	 *
	 * @param bentoStates the {@code List<BentoState>} to map.
	 * @return the {@link DockingLayoutDto} mapped from the
	 * {@code List<BentoState>}.
	 */
	public static DockingLayoutDto toDto(
			final List<BentoState> bentoStates
	) {
		requireNonNull(bentoStates);

		final DockingLayoutDto dockingLayoutDto = new DockingLayoutDto();
		dockingLayoutDto.metadata = createMetadata();

		for (final BentoState state : bentoStates) {
			dockingLayoutDto.bentoStates.add(toDto(state));
		}

		return dockingLayoutDto;
	}

	/**
	 * Creates metadata for a persisted layout payload.
	 *
	 * @return the metadata DTO.
	 */
	private static LayoutMetadataDto createMetadata() {
		final LayoutMetadataDto metadata = new LayoutMetadataDto();
		metadata.schemaVersion = DockingLayoutDto.getCurrentSchemaVersion();
		return metadata;
	}

	/**
	 * Maps a {@link BentoState} to a {@link BentoStateDto}.
	 *
	 * @param bentoState the {@link BentoState} to map.
	 *
	 * @return the {@link BentoState} mapped from the {@link BentoStateDto}.
	 */
	public static BentoStateDto toDto(
			final BentoState bentoState
	) {
		requireNonNull(bentoState);

		final BentoStateDto bentoStateDto = new BentoStateDto();

		bentoStateDto.identifier = bentoState.getIdentifier();

		for (final DockContainerRootBranchState rootBranchState :
				bentoState.getRootBranchStates()) {
			bentoStateDto.rootBranches.add(toDto(rootBranchState));
		}

		for (final DragDropStageState stage :
				bentoState.getDragDropStageStates()) {
			bentoStateDto.dragDropStages.add(toDto(stage));
		}

		return bentoStateDto;
	}

	/**
	 * Maps a {@link DockContainerRootBranchState} to a
	 * {@link DockContainerRootBranchDto}.
	 *
	 * @param root the {@link DockContainerRootBranchState} to map.
	 *
	 * @return the {@link DockContainerRootBranchDto} mapped from the
	 * {@link DockContainerRootBranchState}.
	 */
	public static DockContainerRootBranchDto toDto(
			final DockContainerRootBranchState root
	) {
		requireNonNull(root);

		final DockContainerRootBranchDto rootBranchDto =
				new DockContainerRootBranchDto();

		rootBranchDto.identifier = root.getIdentifier();

		root.doPruneWhenEmpty().ifPresent(pruneWhenEmpty ->
				rootBranchDto.pruneWhenEmpty = pruneWhenEmpty
		);

		root.getOrientation().ifPresent(orientation ->
				rootBranchDto.orientation = orientation
		);

		root.getDividerPositions().forEach((index, position) -> {
			final DividerPositionDto dividerPositionDto =
					new DividerPositionDto();
			dividerPositionDto.index = index;
			dividerPositionDto.position = position;
			rootBranchDto.dividerPositions.add(dividerPositionDto);
		});

		for (final DockContainerState dockContainerState :
				root.getChildDockContainerStates()) {

			switch (dockContainerState) {

				case final DockContainerBranchState dockContainerBranchState -> rootBranchDto.childDockContainers.add(
						toDto(dockContainerBranchState)
				);

				case final DockContainerLeafState dockContainerLeafState -> rootBranchDto.childDockContainers.add(
						toDto(dockContainerLeafState)
				);
			}
		}

		return rootBranchDto;
	}

	/**
	 * Maps a {@link DragDropStageState} to a {@link DragDropStageDto}.
	 *
	 * @param stageState the {@link DragDropStageState} to map.
	 *
	 * @return the {@link DragDropStageDto} mapped from the
	 * {@link DragDropStageState}.
	 */
	public static DragDropStageDto toDto(
			final DragDropStageState stageState
	) {
		final DragDropStageDto stageDto = new DragDropStageDto();
		stageDto.autoCloseWhenEmpty = stageState.isAutoClosedWhenEmpty();
		stageDto.title = stageState.getTitle().orElse(null);
		stageDto.x = stageState.getX().orElse(null);
		stageDto.y = stageState.getY().orElse(null);
		stageDto.width = stageState.getWidth().orElse(null);
		stageDto.height = stageState.getHeight().orElse(null);
		stageDto.opacity = stageState.getOpacity().orElse(null);
		stageDto.iconified = stageState.isIconified().orElse(null);
		stageDto.fullScreen = stageState.isFullScreen().orElse(null);
		stageDto.maximized = stageState.isMaximized().orElse(null);
		stageDto.alwaysOnTop = stageState.isAlwaysOnTop().orElse(null);
		stageDto.resizable = stageState.isResizable().orElse(null);
		stageDto.showing = stageState.isShowing().orElse(null);
		stageDto.focused = stageState.isFocused().orElse(null);
		stageDto.modality = stageState.getModality().orElse(null);
		stageState.getDockContainerRootBranchState().ifPresent(
				dockContainerRootBranchState ->
						stageDto.dockContainerRootBranchDto =
								toDto(dockContainerRootBranchState));
		return stageDto;
	}

	/**
	 * Maps a {@link DockableState} to a {@link DockableDto}.
	 *
	 * <p>The node, the factories, and the consumer are left out because the
	 * application supplies those at the time of during restoration.</p>
	 *
	 * @param dockableState the {@link DockableState} to map.
	 *
	 * @return the {@link DockableDto} mapped from the {@link DockableState}.
	 */
	public static DockableDto toDto(final DockableState dockableState) {

		final DockableDto dockableDto = new DockableDto();
		dockableDto.identifier = dockableState.getIdentifier();
		dockableDto.title = dockableState.getTitle().orElse(null);
		dockableDto.tooltipText = dockableState.getTooltipText().orElse(null);
		dockableDto.dragGroupMask = dockableState.getDragGroupMask().orElse(null);
		dockableDto.isClosable = dockableState.isClosable().orElse(null);
		return dockableDto;
	}

	/**
	 * Maps a {@link DockContainerBranchState} to a {@link DockContainerBranchDto}.
	 *
	 * @param branchState the {@link DockContainerBranchState} to map.
	 *
	 * @return the {@link DockContainerBranchDto} mapped from the
	 * {@link DockContainerBranchState}.
	 */
	public static DockContainerBranchDto toDto(
			final DockContainerBranchState branchState
	) {
		final DockContainerBranchDto branchDto = new DockContainerBranchDto();

		branchDto.identifier = branchState.getIdentifier();

		branchState.doPruneWhenEmpty().ifPresent(pruneWhenEmpty ->
				branchDto.pruneWhenEmpty = pruneWhenEmpty
		);

		branchDto.orientation =
				branchState.getOrientation().orElse(null);

		branchState.getDividerPositions().forEach((index, position) -> {
			final DividerPositionDto dividerPositionDto = new DividerPositionDto();
			dividerPositionDto.index = index;
			dividerPositionDto.position = position;
			branchDto.dividerPositions.add(dividerPositionDto);
		});

		for (final DockContainerState child :
				branchState.getChildDockContainerStates()) {
			if (child instanceof final DockContainerBranchState childBranchState) {
				branchDto.childDockContainers.add(toDto(childBranchState));
			} else if (child instanceof final DockContainerLeafState childLeafState) {
				branchDto.childDockContainers.add(toDto(childLeafState));
			}
		}

		return branchDto;
	}

	/**
	 * Maps a {@link DockContainerLeafState} to a {@link DockContainerLeafDto}.
	 *
	 * @param leafState the {@link DockContainerLeafState} to map.
	 *
	 * @return the {@link DockContainerLeafDto} mapped from the
	 * {@link DockContainerLeafState}.
	 */
	public static DockContainerLeafDto toDto(
			final DockContainerLeafState leafState
	) {
		final DockContainerLeafDto leafDto = new DockContainerLeafDto();

		leafDto.identifier = leafState.getIdentifier();

		leafState.doPruneWhenEmpty().ifPresent(pruneWhenEmpty ->
				leafDto.pruneWhenEmpty = pruneWhenEmpty
		);

		leafDto.selectedDockableIdentifier =
				leafState.getSelectedDockableIdentifier()
						.orElse(null);

		leafDto.side = leafState.getSide().orElse(null);

		leafDto.isResizableWithParent =
				leafState.isResizableWithParent().orElse(null);

		leafDto.isCanSplit = leafState.isCanSplit().orElse(null);

		leafState.getUncollapsedSizePx().ifPresent(uncollapsedSizePx ->
				leafDto.uncollapsedSizePx = uncollapsedSizePx
		);

		leafState.isCollapsed().ifPresent(isCollapsed ->
				leafDto.isCollapsed = isCollapsed
		);

		for (final DockableState d : leafState.getChildDockableStates()) {
			DockableDto dockableDto = toDto(d);
			leafDto.dockables.add(dockableDto);
		}

		return leafDto;
	}

	/**
	 * Maps a {@link DockingLayoutDto} to a {@code List<BentoState>}.
	 *
	 * @param dockingLayoutDto the {@link DockingLayoutDto} to map.
	 *
	 * @return the {@code List<BentoState>} mapped from the
	 * {@link DockingLayoutDto}.
	 */
	public static List<BentoState> fromDto(
			final DockingLayoutDto dockingLayoutDto
	) throws BentoStateException {
		requireNonNull(dockingLayoutDto);

		validateSupportedMetadata(dockingLayoutDto.metadata);

		final List<BentoState> bentoStateList = new ArrayList<>();

		for (final BentoStateDto stateDto : dockingLayoutDto.bentoStates) {
			bentoStateList.add(fromDto(stateDto));
		}

		return bentoStateList;
	}

	/**
	 * Validates that decoded layout metadata can be restored by this version of
	 * the persistence framework.
	 *
	 * @param metadata the decoded metadata. {@code null} indicates a legacy
	 * payload that did not include metadata.
	 *
	 * @throws BentoStateException when the metadata describes an unsupported
	 * schema version.
	 */
	static void validateSupportedMetadata(
			final @Nullable LayoutMetadataDto metadata
	) throws BentoStateException {
		if (metadata == null || metadata.schemaVersion == null) {
			return;
		}

		if (metadata.schemaVersion < 1) {
			throw new BentoStateException(
					"Unsupported BentoFX docking layout schema version: "
							+ metadata.schemaVersion
			);
		}

		if (metadata.schemaVersion > DockingLayoutDto.getCurrentSchemaVersion()) {
			throw new BentoStateException(
					"Unsupported BentoFX docking layout schema version: "
							+ metadata.schemaVersion
							+ ". Current supported schema version is "
							+ DockingLayoutDto.getCurrentSchemaVersion()
			);
		}
	}

	/**
	 * Maps a {@link BentoStateDto} to a {@link BentoState}.
	 *
	 * @param bentoStateDto the {@link BentoStateDto} to map.
	 *
	 * @return the {@link BentoState} mapped from the {@link BentoStateDto}.
	 *
	 * @throws BentoStateException when the DTO carries no Bento identifier.
	 */
	public static BentoState fromDto(
			final BentoStateDto bentoStateDto
	) throws BentoStateException {
		requireNonNull(bentoStateDto);

		// This identifier comes from a decoded payload, so a missing one is
		// malformed input rather than a programming error.
		if (bentoStateDto.identifier == null) {
			throw new BentoStateException(
					"Cannot restore a Bento that has no identifier"
			);
		}

		final BentoStateBuilder builder =
				new BentoStateBuilder(bentoStateDto.identifier);

		for (final DockContainerRootBranchDto dto : bentoStateDto.rootBranches) {
			builder.addRootBranchState(fromDto(dto));
		}

		for (final DragDropStageDto dto : bentoStateDto.dragDropStages) {
			builder.addDragDropStageState(fromDto(dto));
		}

		return builder.build();
	}

	/**
	 * Maps a {@link DockContainerRootBranchDto} to a
	 * {@link DockContainerRootBranchState}.
	 *
	 * @param rootBranchDto the {@link DockContainerRootBranchDto} to map.
	 *
	 * @return the {@link DockContainerRootBranchState} mapped from the
	 * {@link DockContainerRootBranchDto}.
	 */
	public static DockContainerRootBranchState fromDto(
			final DockContainerRootBranchDto rootBranchDto
	) {
		requireNonNull(rootBranchDto);

		final String id = rootBranchDto.identifier != null ?
				rootBranchDto.identifier :
				ROOT_BRANCH_ELEMENT_NAME;

		final DockContainerRootBranchStateBuilder builder =
				new DockContainerRootBranchStateBuilder(id);

		builder.setOrientation(rootBranchDto.orientation)
				.setPruneWhenEmpty(rootBranchDto.pruneWhenEmpty);

		for (final DividerPositionDto position : rootBranchDto.dividerPositions) {
			if (position.index != null && position.position != null) {
				builder.addDividerPosition(position.index, position.position);
			}
		}

		addDockContainers(
				builder::addDockContainerState, rootBranchDto.childDockContainers
		);

		return builder.build();
	}

	/**
	 * Maps a {@link DragDropStageDto} to a {@link DragDropStageState}.
	 *
	 * @param stageDto the {@link DragDropStageDto} to map.
	 *
	 * @return the {@link DragDropStageState} mapped from the
	 * {@link DragDropStageDto}.
	 */
	public static DragDropStageState fromDto(
			final DragDropStageDto stageDto
	) {
		requireNonNull(stageDto);

		final DragDropStageStateBuilder builder = new DragDropStageStateBuilder(
				Boolean.TRUE.equals(stageDto.autoCloseWhenEmpty)
		)
				.setTitle(stageDto.title)
				.setX(stageDto.x)
				.setY(stageDto.y)
				.setWidth(stageDto.width)
				.setHeight(stageDto.height)
				.setModality(stageDto.modality)
				.setOpacity(stageDto.opacity)
				.setIconified(stageDto.iconified)
				.setFullScreen(stageDto.fullScreen)
				.setMaximized(stageDto.maximized)
				.setAlwaysOnTop(stageDto.alwaysOnTop)
				.setResizable(stageDto.resizable)
				.setShowing(stageDto.showing)
				.setFocused(stageDto.focused);

		if (stageDto.dockContainerRootBranchDto != null) {
			builder.setDockContainerRootBranchState(
					fromDto(stageDto.dockContainerRootBranchDto)
			);
		}

		return builder.build();
	}

	/**
	 * Maps a {@link DockContainerBranchDto} to a
	 * {@link DockContainerBranchState}.
	 *
	 * @param branchDto the {@link DockContainerBranchDto} to map.
	 *
	 * @return the {@link DockContainerBranchState} mapped from the
	 * {@link DockContainerBranchDto}.
	 */
	public static DockContainerBranchState fromDto(
			final DockContainerBranchDto branchDto
	) {

		requireNonNull(branchDto);

		final String id = branchDto.identifier != null ?
				branchDto.identifier :
				BRANCH_ELEMENT_NAME;

		final DockContainerBranchStateBuilder builder =
				new DockContainerBranchStateBuilder(id);
		builder.setPruneWhenEmpty(branchDto.pruneWhenEmpty);
		setOrientation(builder, branchDto.orientation);

		for (final DividerPositionDto position : branchDto.dividerPositions) {
			if (position.index != null && position.position != null) {
				builder.addDividerPosition(position.index, position.position);
			}
		}

		addDockContainers(builder::addDockContainerState, branchDto.childDockContainers);
		return builder.build();
	}

	/**
	 * Sets the orientation of the {@link DockContainerBranchStateBuilder}. Logs
	 * a warning if the {@code orientation} is not a valid {@link Orientation}
	 * value.
	 *
	 * @param builder the {@link DockContainerBranchStateBuilder} whose
	 * orientation is to be set.
	 * @param orientation the {@link String} value of the {@link Orientation}
	 * being set.
	 */
	private static void setOrientation(
			final DockContainerBranchStateBuilder builder,
			final @Nullable Orientation orientation
	) {
		try {

			builder.setOrientation(orientation);
		} catch (final Exception e) {

			logger.warn(
					"Could not determine the orientation for {}.", orientation,
					e
			);
		}
	}

	/**
	 * Maps each {@link DockContainerDto} to its state and hands it to
	 * {@code addDockContainerState}, preserving the DTO's child order.
	 *
	 * <p>A {@link Consumer}, rather than a builder, because
	 * {@link DockContainerRootBranchStateBuilder} delegates to
	 * {@link DockContainerBranchStateBuilder} instead of extending it, leaving a
	 * method reference as all the two share.</p>
	 *
	 * @param addDockContainerState the builder mutator each mapped child is
	 * passed to.
	 * @param dockContainers the {@link DockContainerDto}s to be added.
	 */
	private static void addDockContainers(
			final Consumer<DockContainerState> addDockContainerState,
			final List<DockContainerDto> dockContainers
	) {
        for (final DockContainerDto container : dockContainers) {
            if (container instanceof final DockContainerBranchDto branch) {
                addDockContainerState.accept(fromDto(branch));
            } else if (container instanceof final DockContainerLeafDto leaf) {
                addDockContainerState.accept(fromDto(leaf));
            }
        }
	}

	/**
	 * Maps a {@link DockContainerLeafDto} to a
	 * {@link DockContainerLeafState}.
	 *
	 * @param leafDto the {@link DockContainerLeafDto} to map.
	 *
	 * @return the {@link DockContainerLeafState} mapped from the
	 * {@link DockContainerLeafDto}.
	 */
	public static DockContainerLeafState fromDto(
			final DockContainerLeafDto leafDto
	) {

		requireNonNull(leafDto);

		final String id = leafDto.identifier != null ?
				leafDto.identifier :
				LEAF_ELEMENT_NAME;

		final DockContainerLeafStateBuilder builder =
				new DockContainerLeafStateBuilder(id)
						.setSelectedDockableStateIdentifier(leafDto.selectedDockableIdentifier)
						.setSide(leafDto.side)
						.setResizableWithParent(leafDto.isResizableWithParent)
						.setCanSplit(leafDto.isCanSplit)
						.setUncollapsedSizePx(leafDto.uncollapsedSizePx)
						.setCollapsed(leafDto.isCollapsed);

		builder.setPruneWhenEmpty(leafDto.pruneWhenEmpty);

		for (final DockableDto dockableDto : leafDto.dockables) {

			if (dockableDto.identifier != null) {

				builder.addChildDockableState(fromDto(dockableDto));
			}
		}

		return builder.build();
	}

	/**
	 * Maps a {@link DockableDto} to a {@link DockableState}.
	 *
	 * @param dockableDto the {@link DockableDto} to map. Its identifier must not
	 * be {@code null}.
	 *
	 * @return the {@link DockableState} mapped from the {@link DockableDto}.
	 */
	public static DockableState fromDto(final DockableDto dockableDto) {

		requireNonNull(dockableDto);

		return new DockableStateBuilder(
				requireNonNull(dockableDto.identifier)
		)
				.setTitle(dockableDto.title)
				.setTooltipText(dockableDto.tooltipText)
				.setDragGroupMask(dockableDto.dragGroupMask)
				.setClosable(dockableDto.isClosable)
				.build();
	}
}
