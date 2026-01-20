/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.common.mapper;

import javafx.geometry.Orientation;
import org.jetbrains.annotations.NotNull;
import software.coley.bentofx.persistence.api.codec.BentoState;
import software.coley.bentofx.persistence.api.codec.DockContainerBranchState;
import software.coley.bentofx.persistence.api.codec.DockContainerLeafState;
import software.coley.bentofx.persistence.api.codec.DockContainerRootBranchState;
import software.coley.bentofx.persistence.api.codec.DockContainerState;
import software.coley.bentofx.persistence.api.codec.DockableState;
import software.coley.bentofx.persistence.api.codec.DragDropStageState;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DividerPositionDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerLeafDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockContainerRootBranchDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DockableDto;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.DragDropStageDto;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Maps between the immutable {@code *State} domain objects and
 * JAXB/Jackson-friendly DTOs.
 *
 * <p>DTOs are intentionally acyclic and preserve child order via {@link List}.</p>
 */
public final class BentoStateMapper {

    private BentoStateMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static @NotNull BentoStateDto toDto(final @NotNull BentoState state) {
        requireNonNull(state);
        final BentoStateDto dto = new BentoStateDto();
        dto.identifier = state.getIdentifier();

        for (final DockContainerRootBranchState root : state.getRootBranchStates()) {
            dto.rootBranches.add(toDto(root));
        }
        return dto;
    }

    public static @NotNull DockContainerRootBranchDto toDto(final @NotNull DockContainerRootBranchState root) {
        requireNonNull(root);
        final DockContainerRootBranchDto dto = new DockContainerRootBranchDto();
        dto.identifier = root.getIdentifier();

        root.getParent().ifPresent(parent -> dto.parentStage = toDto(parent));

        for (final DockContainerBranchState b : root.getDockContainerBranchStates()) {
            dto.branches.add(toDto(b));
        }
        for (final DockContainerLeafState l : root.getDockContainerLeafStates()) {
            dto.leaves.add(toDto(l));
        }

        return dto;
    }

    public static @NotNull DragDropStageDto toDto(final @NotNull DragDropStageState s) {
        final DragDropStageDto dto = new DragDropStageDto();
        dto.autoCloseWhenEmpty = s.isAutoClosedWhenEmpty();
        dto.x = s.getX().orElse(null);
        dto.y = s.getY().orElse(null);
        dto.width = s.getWidth().orElse(null);
        dto.height = s.getHeight().orElse(null);
        dto.iconified = s.isIconified().orElse(null);
        dto.fullScreen = s.isFullScreen().orElse(null);
        dto.maximized = s.isMaximized().orElse(null);
        return dto;
    }

    public static @NotNull DockContainerBranchDto toDto(final @NotNull DockContainerBranchState state) {
        final DockContainerBranchDto dto = new DockContainerBranchDto();
        dto.identifier = state.getIdentifier();
        dto.orientation = state.getOrientation().map(Enum::name).orElse(null);

        state.getDividerPositions().forEach((idx, pos) -> {
            final DividerPositionDto d = new DividerPositionDto();
            d.index = idx;
            d.position = pos;
            dto.dividerPositions.add(d);
        });

        for (final DockContainerState child : state.getDockContainerStates()) {
            if (child instanceof final DockContainerBranchState b) {
                dto.children.add(toDto(b));
            } else if (child instanceof final DockContainerLeafState l) {
                dto.children.add(toDto(l));
            }
        }

        return dto;
    }

    public static @NotNull DockContainerLeafDto toDto(final @NotNull DockContainerLeafState state) {
        final DockContainerLeafDto dto = new DockContainerLeafDto();
        dto.identifier = state.getIdentifier();
        dto.side = state.getSide().orElse(null);
        dto.selectedDockableId = state.getSelectedDockableStateIdentifier().orElse(null);

        for (final DockableState d : state.getDockableStates()) {
            final DockableDto dd = new DockableDto();
            dd.identifier = d.getIdentifier();
            dto.dockables.add(dd);
        }
        return dto;
    }

    public static @NotNull BentoState fromDto(final @NotNull BentoStateDto dto) {
        requireNonNull(dto);
        final String id = dto.identifier != null ? dto.identifier : "bento";
        final BentoState.BentoStateBuilder builder = new BentoState.BentoStateBuilder(id);

        if (dto.rootBranches != null) {
            for (final DockContainerRootBranchDto rootDto : dto.rootBranches) {
                builder.addRootBranchState(fromDto(rootDto));
            }
        }
        return builder.build();
    }

    public static @NotNull DockContainerRootBranchState fromDto(final @NotNull DockContainerRootBranchDto dto) {
        final String id = dto.identifier != null ? dto.identifier : "root-branch";
        final DockContainerRootBranchState.DockContainerRootBranchStateBuilder builder =
                new DockContainerRootBranchState.DockContainerRootBranchStateBuilder(id);

        if (dto.parentStage != null) {
            builder.setParent(fromDto(dto.parentStage));
        }

        if (dto.branches != null) {
            for (final DockContainerBranchDto b : dto.branches) {
                builder.addDockContainerBranchState(fromDto(b));
            }
        }
        if (dto.leaves != null) {
            for (final DockContainerLeafDto l : dto.leaves) {
                builder.addDockContainerLeafState(fromDto(l));
            }
        }

        return builder.build();
    }

    public static @NotNull DragDropStageState fromDto(final @NotNull DragDropStageDto dto) {
        final DragDropStageState.DragDropStageStateBuilder b =
                new DragDropStageState.DragDropStageStateBuilder(Boolean.TRUE.equals(dto.autoCloseWhenEmpty))
                        .setTitle(dto.title)
                        .setX(dto.x)
                        .setY(dto.y)
                        .setWidth(dto.width)
                        .setHeight(dto.height)
                        .setIsIconified(dto.iconified)
                        .setIsFullScreen(dto.fullScreen)
                        .setIsMaximized(dto.maximized)
                        // Avoid cycles
                        .setDockContainerRootBranchState(null);

        return b.build();
    }

    public static @NotNull DockContainerBranchState fromDto(final @NotNull DockContainerBranchDto dto) {
        final String id = dto.identifier != null ? dto.identifier : "branch";
        final DockContainerBranchState.DockContainerBranchStateBuilder builder =
                new DockContainerBranchState.DockContainerBranchStateBuilder(id);

        if (dto.orientation != null) {
            try {
                builder.setOrientation(Orientation.valueOf(dto.orientation));
            } catch (final Exception ignored) {
            }
        }

        if (dto.dividerPositions != null) {
            for (final DividerPositionDto d : dto.dividerPositions) {
                if (d != null && d.index != null && d.position != null) {
                    builder.addDividerPosition(d.index, d.position);
                }
            }
        }

        if (dto.children != null) {
            for (final DockContainerDto child : dto.children) {
                if (child instanceof final DockContainerBranchDto b) {
                    builder.addDockContainerState(fromDto(b));
                } else if (child instanceof final DockContainerLeafDto l) {
                    builder.addDockContainerState(fromDto(l));
                }
            }
        }

        return builder.build();
    }

    public static @NotNull DockContainerLeafState fromDto(final @NotNull DockContainerLeafDto dto) {
        final String id = dto.identifier != null ? dto.identifier : "leaf";
        final DockContainerLeafState.DockContainerLeafStateBuilder builder =
                new DockContainerLeafState.DockContainerLeafStateBuilder(id);

        if (dto.selectedDockableId != null) {
            builder.setSelectedDockableStateIdentifier(dto.selectedDockableId);
        }

        if (dto.side != null) {
            builder.setSide(dto.side);
        }

        if (dto.dockables != null) {
            for (final DockableDto d : dto.dockables) {
                if (d != null && d.identifier != null) {
                    builder.addDockableState(new DockableState.DockableStateBuilder(d.identifier).build());
                }
            }
        }

        return builder.build();
    }
}
