/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper;

import javafx.geometry.Orientation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.coley.bentofx.persistence.api.codec.*;
import software.coley.bentofx.persistence.api.codec.BentoState.BentoStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerBranchState.DockContainerBranchStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerLeafState.DockContainerLeafStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockContainerRootBranchState.DockContainerRootBranchStateBuilder;
import software.coley.bentofx.persistence.api.codec.DockableState.DockableStateBuilder;
import software.coley.bentofx.persistence.api.codec.DragDropStageState.DragDropStageStateBuilder;
import software.coley.bentofx.persistence.api.codec.IdentifiableStageState.StageStateBuilder;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.*;

import java.util.ArrayList;
import java.util.List;

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
     * Maps a {@code List<BentoState>} to a {@link BentoStateListDto}.
     *
     * @param bentoStates the {@code List<BentoState>} to map.
     * @return the {@link BentoStateListDto} mapped from the
     * {@code List<BentoState>}.
     */
    public static @NotNull BentoStateListDto toDto(
            final @NotNull List<@NotNull BentoState> bentoStates
    ) {
        final BentoStateListDto stateDtoList = new BentoStateListDto();

        for (final BentoState state : bentoStates) {
            stateDtoList.getBentoStates().add(toDto(state));
        }

        return stateDtoList;
    }

    /**
     * Maps a {@link BentoState} to a {@link BentoStateDto}.
     *
     * @param state the {@link BentoState} to map.
     * @return the {@link BentoState} mapped from the {@link BentoStateDto}.
     */
    public static @NotNull BentoStateDto toDto(
            final @NotNull BentoState state
    ) {
        requireNonNull(state);

        final BentoStateDto bentoStateDto = new BentoStateDto();

        bentoStateDto.identifier = state.getIdentifier();

        for (final IdentifiableStageState identifiableStageState : state.getIdentifiableStageStates()) {
            bentoStateDto.identifiableStages.add(toDto(identifiableStageState));
        }

        for (final DragDropStageState stage : state.getDragDropStageStates()) {
            bentoStateDto.dragDropStages.add(toDto(stage));
        }

        return bentoStateDto;
    }

    /**
     * Maps a {@link DockContainerRootBranchState} to a
     * {@link DockContainerRootBranchDto}.
     *
     * @param root the {@link DockContainerRootBranchState} to map.
     * @return the {@link DockContainerRootBranchDto} mapped from the
     * {@link DockContainerRootBranchState}.
     */
    public static @NotNull DockContainerRootBranchDto toDto(
            final @NotNull DockContainerRootBranchState root
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

        root.getParent().ifPresent(parent ->
                rootBranchDto.parentStage = toDto(parent)
        );

        root.getDividerPositions().forEach((index, position) -> {
            final DividerPositionDto dividerPositionDto = new DividerPositionDto();
            dividerPositionDto.index = index;
            dividerPositionDto.position = position;
            rootBranchDto.dividerPositions.add(dividerPositionDto);
        });

        for (final DockContainerState dockContainerState :
                root.getChildDockContainerStates()) {

            switch (dockContainerState) {

                case final DockContainerBranchState dockContainerBranchState ->
                        rootBranchDto.branches.add(toDto(dockContainerBranchState));

                case final DockContainerLeafState dockContainerLeafState ->
                        rootBranchDto.leaf = toDto(dockContainerLeafState);

                default -> logger.warn(
                        "Unsupported DockContainerBranchState type: {}",
                        dockContainerState
                );
            }
        }

        return rootBranchDto;
    }

    /**
     * Maps a {@link IdentifiableStageState} to a {@link IdentifiableStageDto}.
     *
     * @param identifiableStageState the {@link IdentifiableStageState} to map.
     * @return the {@link IdentifiableStageDto} mapped from the {@link IdentifiableStageState}.
     */
    public static @NotNull IdentifiableStageDto toDto(
            final @NotNull IdentifiableStageState identifiableStageState
    ) {
        final IdentifiableStageDto identifiableStageDto = new IdentifiableStageDto();
        identifiableStageDto.identifier = identifiableStageState.getIdentifier();
        identifiableStageDto.title = identifiableStageState.getTitle().orElse(null);
        identifiableStageDto.x = identifiableStageState.getX().orElse(null);
        identifiableStageDto.y = identifiableStageState.getY().orElse(null);
        identifiableStageDto.width = identifiableStageState.getWidth().orElse(null);
        identifiableStageDto.height = identifiableStageState.getHeight().orElse(null);
        identifiableStageDto.opacity = identifiableStageState.getOpacity().orElse(null);
        identifiableStageDto.iconified = identifiableStageState.isIconified().orElse(null);
        identifiableStageDto.fullScreen = identifiableStageState.isFullScreen().orElse(null);
        identifiableStageDto.maximized = identifiableStageState.isMaximized().orElse(null);
        identifiableStageDto.alwaysOnTop = identifiableStageState.isAlwaysOnTop().orElse(null);
        identifiableStageDto.resizable = identifiableStageState.isResizable().orElse(null);
        identifiableStageDto.showing = identifiableStageState.isShowing().orElse(null);
        identifiableStageDto.showing = identifiableStageState.isFocused().orElse(null);
        identifiableStageDto.modality = identifiableStageState.getModality().orElse(null);

        for (DockContainerRootBranchState rootBranchState :
                identifiableStageState.getRootBranchStates()) {
            identifiableStageDto.dockContainerRootBranches.add(toDto(rootBranchState));
        }

        return identifiableStageDto;
    }

    /**
     * Maps a {@link DragDropStageState} to a {@link DragDropStageDto}.
     *
     * @param stageState the {@link DragDropStageState} to map.
     * @return the {@link DragDropStageDto} mapped from the
     * {@link DragDropStageState}.
     */
    public static @NotNull DragDropStageDto toDto(
            final @NotNull DragDropStageState stageState
    ) {
        final DragDropStageDto stageDto = new DragDropStageDto();
        stageDto.identifier = stageState.getIdentifier();
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
        stageDto.showing = stageState.isFocused().orElse(null);
        stageDto.modality = stageState.getModality().orElse(null);
        stageState.getDockContainerRootBranchState().ifPresent(
                dockContainerRootBranchState ->
                        stageDto.dockContainerRootBranchDto =
                                toDto(dockContainerRootBranchState));
        return stageDto;
    }

    public static @NotNull DockableDto toDto(DockableState dockableState) {

        final DockableDto dockableDto = new DockableDto();
        dockableDto.identifier = dockableState.getIdentifier();
        return dockableDto;
    }

    /**
     * Maps a {@link DockContainerBranchState} to a {@link DockContainerBranchDto}.
     *
     * @param branchState the {@link DockContainerBranchState} to map.
     * @return the {@link DockContainerBranchDto} mapped from the
     * {@link DockContainerBranchState}.
     */
    public static @NotNull DockContainerBranchDto toDto(
            final @NotNull DockContainerBranchState branchState
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

        for (final DockContainerState child : branchState.getChildDockContainerStates()) {
            if (child instanceof final DockContainerBranchState childBranchState) {
                branchDto.children.add(toDto(childBranchState));
            } else if (child instanceof final DockContainerLeafState childLeafState) {
                branchDto.children.add(toDto(childLeafState));
            }
        }

        return branchDto;
    }

    /**
     * Maps a {@link DockContainerLeafState} to a {@link DockContainerLeafDto}.
     *
     * @param leafState the {@link DockContainerLeafState} to map.
     * @return the {@link DockContainerLeafDto} mapped from the
     * {@link DockContainerLeafState}.
     */
    public static @NotNull DockContainerLeafDto toDto(
            final @NotNull DockContainerLeafState leafState
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
     * Maps a {@link BentoStateListDto} to a {@code List<BentoState>}.
     *
     * @param bentoStateListDto the {@link BentoStateListDto} to map.
     * @return the {@code List<BentoState>} mapped from the
     * {@link BentoStateListDto}.
     */
    public static @NotNull List<@NotNull BentoState> fromDto(
            final @NotNull BentoStateListDto bentoStateListDto
    ) {
        final List<BentoState> bentoStateList = new ArrayList<>();

        for (final BentoStateDto stateDto : bentoStateListDto.getBentoStates()) {
            bentoStateList.add(fromDto(stateDto));
        }

        return bentoStateList;
    }

    /**
     * Maps a {@link BentoStateDto} to a {@link BentoState}.
     *
     * @param bentoStateDto the {@link BentoStateDto} to map.
     * @return the {@link BentoState} mapped from the {@link BentoStateDto}.
     */
    public static @NotNull BentoState fromDto(
            final @NotNull BentoStateDto bentoStateDto
    ) {
        requireNonNull(bentoStateDto);

        final BentoStateBuilder builder =
                new BentoStateBuilder(bentoStateDto.identifier);

        if (bentoStateDto.identifiableStages != null) {
            for (final IdentifiableStageDto dto : bentoStateDto.identifiableStages) {
                builder.addIdentifiableStageState(fromDto(dto));
            }
        }

        if (bentoStateDto.dragDropStages != null) {
            for (final DragDropStageDto dto : bentoStateDto.dragDropStages) {
                builder.addDragDropStageState(fromDto(dto));
            }
        }

        return builder.build();
    }

    /**
     * Maps a {@link DockContainerRootBranchDto} to a
     * {@link DockContainerRootBranchState}.
     *
     * @param rootBranchDto the {@link DockContainerRootBranchDto} to map.
     * @return the {@link DockContainerRootBranchState} mapped from the
     * {@link DockContainerRootBranchDto}.
     */
    public static @NotNull DockContainerRootBranchState fromDto(
            final @NotNull DockContainerRootBranchDto rootBranchDto
    ) {
        final String id = rootBranchDto.identifier != null ?
                rootBranchDto.identifier :
                ROOT_BRANCH_ELEMENT_NAME;

        final DockContainerRootBranchStateBuilder builder =
                new DockContainerRootBranchStateBuilder(id);

        if (rootBranchDto.parentStage != null) {
            final IdentifiableStageState parentIdentifiableStageState =
                    fromDto(rootBranchDto.parentStage);
            builder.setParentStage(parentIdentifiableStageState);
        }

        builder.setOrientation(rootBranchDto.orientation)
                .setPruneWhenEmpty(rootBranchDto.pruneWhenEmpty);

        setDividerPositions(builder, rootBranchDto.dividerPositions);

        if (rootBranchDto.branches != null) {
            for (final DockContainerBranchDto branchDto : rootBranchDto.branches) {
                builder.addDockContainerState(fromDto(branchDto));
            }
        }

        if (rootBranchDto.leaf != null) {
            builder.addDockContainerState(fromDto(rootBranchDto.leaf));
        }

        return builder.build();
    }

    /**
     * Maps a {@link IdentifiableStageDto} to a {@link IdentifiableStageState}.
     *
     * @param identifiableStageDto the {@link IdentifiableStageDto} to map.
     * @return the {@link IdentifiableStageState} mapped from the {@link IdentifiableStageDto}.
     */
    public static @NotNull IdentifiableStageState fromDto(
            final @NotNull IdentifiableStageDto identifiableStageDto
    ) {
        final StageStateBuilder builder = new StageStateBuilder(
                identifiableStageDto.identifier
        )
                .setTitle(identifiableStageDto.title)
                .setX(identifiableStageDto.x)
                .setY(identifiableStageDto.y)
                .setWidth(identifiableStageDto.width)
                .setHeight(identifiableStageDto.height)
                .setModality(identifiableStageDto.modality)
                .setOpacity(identifiableStageDto.opacity)
                .setIconified(identifiableStageDto.iconified)
                .setFullScreen(identifiableStageDto.fullScreen)
                .setMaximized(identifiableStageDto.maximized)
                .setAlwaysOnTop(identifiableStageDto.alwaysOnTop)
                .setResizable(identifiableStageDto.resizable)
                .setShowing(identifiableStageDto.showing)
                .setFocused(identifiableStageDto.focused);

        for (final DockContainerRootBranchDto rootBranchDto :
                identifiableStageDto.dockContainerRootBranches) {
            builder.addRootBranchState(fromDto(rootBranchDto));
        }

        return builder.build();
    }

    /**
     * Maps a {@link DragDropStageDto} to a {@link DragDropStageState}.
     *
     * @param stageDto the {@link DragDropStageDto} to map.
     * @return the {@link DragDropStageState} mapped from the
     * {@link DragDropStageDto}.
     */
    public static @NotNull DragDropStageState fromDto(
            final @NotNull DragDropStageDto stageDto
    ) {
        return new DragDropStageStateBuilder(
                stageDto.identifier,
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
                .setFocused(stageDto.focused)
                .setDockContainerRootBranchState(
                        fromDto(stageDto.dockContainerRootBranchDto)
                )
                .build();
    }

    /**
     * Maps a {@link DockContainerBranchDto} to a
     * {@link DockContainerBranchState}.
     *
     * @param branchDto the {@link DockContainerBranchDto} to map.
     * @return the {@link DockContainerBranchState} mapped from the
     * {@link DockContainerBranchDto}.
     */
    public static @NotNull DockContainerBranchState fromDto(
            final @NotNull DockContainerBranchDto branchDto
    ) {

        final String id = branchDto.identifier != null ?
                branchDto.identifier :
                BRANCH_ELEMENT_NAME;

        final DockContainerBranchStateBuilder builder =
                new DockContainerBranchStateBuilder(id);
        builder.setPruneWhenEmpty(branchDto.pruneWhenEmpty);
        setOrientation(builder, branchDto.orientation);
        setDividerPositions(builder, branchDto.dividerPositions);
        addDockContainers(builder, branchDto.children);
        return builder.build();
    }

    /**
     * Sets the orientation of the {@link DockContainerBranchStateBuilder}. Logs
     * a warning if the {@code orientation} is not a valid {@link Orientation}
     * value.
     *
     * @param builder     the {@link DockContainerBranchStateBuilder} whose
     *                    orientation is to be set.
     * @param orientation the {@link String} value of the {@link Orientation}
     *                    being set.
     */
    private static void setOrientation(
            final @NotNull DockContainerBranchStateBuilder builder,
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
     * Sets the divider positions of the {@link DockContainerBranchStateBuilder}.
     *
     * @param builder          the {@link DockContainerBranchStateBuilder} whose
     *                         divider positions are to be set.
     * @param dividerPositions the positions of the dividers.
     */
    private static void setDividerPositions(
            final @NotNull DockContainerBranchStateBuilder builder,
            final @Nullable List<@Nullable DividerPositionDto> dividerPositions
    ) {
        if (dividerPositions != null) {
            for (final DividerPositionDto d : dividerPositions) {
                if (d != null && d.index != null && d.position != null) {
                    builder.addDividerPosition(d.index, d.position);
                }
            }
        }
    }

    /**
     * Adds the {@link DockContainerDto}s to the
     * {@link DockContainerBranchStateBuilder}.
     *
     * @param builder        the {@link DockContainerBranchStateBuilder} to which
     *                       the {@link DockContainerDto} are to be added.
     * @param dockContainers the {@link DockContainerDto}s to be added.
     */
    private static void addDockContainers(
            final @NotNull DockContainerBranchStateBuilder builder,
            final @Nullable List<@Nullable DockContainerDto> dockContainers
    ) {
        if (dockContainers != null) {
            for (final DockContainerDto container : dockContainers) {
                if (container instanceof final DockContainerBranchDto b) {
                    builder.addDockContainerState(fromDto(b));
                } else if (container instanceof final DockContainerLeafDto l) {
                    builder.addDockContainerState(fromDto(l));
                }
            }
        }
    }

    /**
     * Maps a {@link DockContainerLeafDto} to a
     * {@link DockContainerLeafState}.
     *
     * @param leafDto the {@link DockContainerLeafDto} to map.
     * @return the {@link DockContainerLeafState} mapped from the
     * {@link DockContainerLeafDto}.
     */
    public static @NotNull DockContainerLeafState fromDto(
            final @NotNull DockContainerLeafDto leafDto
    ) {
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

        if (leafDto.dockables != null) {

            for (final DockableDto d : leafDto.dockables) {

                if (d != null && d.identifier != null) {

                    builder.addChildDockableState(
                            new DockableStateBuilder(d.identifier).build()
                    );
                }
            }
        }

        return builder.build();
    }
}
